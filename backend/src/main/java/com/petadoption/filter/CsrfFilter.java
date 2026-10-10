package com.petadoption.filter;

import com.petadoption.util.JsonUtil;
import com.petadoption.util.SecurityUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

@WebFilter("/api/*")
public class CsrfFilter implements Filter {

    private static final Set<String> EXCLUDED_PATHS = Set.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/pets/public"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String method = req.getMethod();
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // Skip CSRF validation for safe read-only methods
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Skip public registration and login (they establish the session/CSRF token)
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                chain.doFilter(request, response);
                return;
            }
        }

        HttpSession session = req.getSession(false);
        if (session != null) {
            String sessionCsrf = (String) session.getAttribute(SecurityUtil.SESSION_CSRF_KEY);
            String headerCsrf = req.getHeader(SecurityUtil.HEADER_CSRF_TOKEN);

            if (sessionCsrf != null && (headerCsrf == null || !sessionCsrf.equals(headerCsrf))) {
                JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF security token.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
