package com.petadoption.config;

import com.petadoption.service.NotificationService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing Online Pet Adoption Platform application context...");
        try {
            DatabaseConfig.initialize();
            NotificationService.getInstance(); // Warm up thread pool
            logger.info("Application context successfully initialized!");
        } catch (Exception e) {
            logger.error("Failed to initialize application context: {}", e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutting down Online Pet Adoption Platform application context...");
        try {
            NotificationService.getInstance().shutdown();
            DatabaseConfig.close();
            logger.info("Application context shut down cleanly.");
        } catch (Exception e) {
            logger.error("Error during context teardown: {}", e.getMessage(), e);
        }
    }
}
