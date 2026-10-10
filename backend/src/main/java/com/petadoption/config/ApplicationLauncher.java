package com.petadoption.config;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.ServerSocket;

/**
 * ApplicationLauncher:
 * Standalone embedded Apache Tomcat 10.1 bootstrap runner.
 * Enables zero-configuration execution via 'mvn compile exec:java'.
 * Serves both the modern Neumorphic frontend and Jakarta Servlets on the exact same HTTP origin.
 */
public class ApplicationLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationLauncher.class);

    public static void main(String[] args) throws Exception {
        int defaultPort = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                defaultPort = Integer.parseInt(portEnv);
            } catch (NumberFormatException ignored) {}
        } else {
            String propPort = System.getProperty("server.port");
            if (propPort != null && !propPort.isBlank()) {
                try {
                    defaultPort = Integer.parseInt(propPort);
                } catch (NumberFormatException ignored) {}
            }
        }

        int port = findAvailablePort(defaultPort);
        if (port != defaultPort) {
            logger.warn("Port {} was in use by another process. Binding to available port: {}", defaultPort, port);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Trigger connector initialization

        File baseDir = new File(System.getProperty("java.io.tmpdir"), "tomcat-pet-adoption-" + port);
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        // Locate webapp directory
        File webappDir = new File("src/main/webapp");
        if (!webappDir.exists()) {
            webappDir = new File("backend/src/main/webapp");
        }
        if (!webappDir.exists()) {
            webappDir = new File("frontend");
        }
        if (!webappDir.exists()) {
            webappDir = new File("../frontend");
        }

        logger.info("Starting Tomcat 10.1 on port {} with document base: {}", port, webappDir.getAbsolutePath());

        Context ctx = tomcat.addWebapp("", webappDir.getAbsolutePath());
        ctx.setParentClassLoader(ApplicationLauncher.class.getClassLoader());

        // Add compiled classes to webapp class loader
        File additionWebInfClasses = new File("target/classes");
        if (!additionWebInfClasses.exists()) {
            additionWebInfClasses = new File("backend/target/classes");
        }

        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        tomcat.start();

        System.out.println("==================================================================");
        System.out.println("  Online Pet Adoption Platform is RUNNING!");
        System.out.println("  Access Platform: http://localhost:" + port);
        System.out.println("  API Root:        http://localhost:" + port + "/api");
        System.out.println("  Embedded Server: Apache Tomcat 10.1 (Jakarta EE 10)");
        System.out.println("==================================================================");

        tomcat.getServer().await();
    }

    private static int findAvailablePort(int preferredPort) {
        if (isPortAvailable(preferredPort)) {
            return preferredPort;
        }
        int[] fallbackPorts = {8085, 8088, 8090, 8081, 9090};
        for (int p : fallbackPorts) {
            if (isPortAvailable(p)) {
                return p;
            }
        }
        return preferredPort;
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
