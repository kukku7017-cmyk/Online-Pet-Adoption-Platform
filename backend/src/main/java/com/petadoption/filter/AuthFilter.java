package com.petadoption.filter;

import com.petadoption.model.User;
import com.petadoption.util.JsonUtil;
import com.petadoption.util.SecurityUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // Pass preflight OPTIONS requests directly
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Allow public routes
        if (isPublicRoute(path, req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Verify session authentication
        User currentUser = SecurityUtil.getAuthenticatedUser(req);
        if (currentUser == null) {
            JsonUtil.sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required. Please log in.");
            return;
        }

        if (!currentUser.isActive()) {
            SecurityUtil.invalidateSession(req);
            JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Your account has been deactivated.");
            return;
        }

        // Role-Based Authorization
        if (path.startsWith("/api/admin")) {
            if (currentUser.getRole() != User.Role.ADMIN) {
                JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Requires Administrator privileges.");
                return;
            }
        } else if (path.startsWith("/api/shelter")) {
            if (currentUser.getRole() != User.Role.SHELTER && currentUser.getRole() != User.Role.ADMIN) {
                JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Requires Shelter privileges.");
                return;
            }
        } else if (path.startsWith("/api/adopter")) {
            if (currentUser.getRole() != User.Role.ADOPTER && currentUser.getRole() != User.Role.ADMIN) {
                JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Requires Adopter privileges.");
                return;
            }
        } else if (path.startsWith("/api/pets/create") || path.startsWith("/api/pets/update") || path.startsWith("/api/pets/my-listings")) {
            if (currentUser.getRole() != User.Role.SHELTER && currentUser.getRole() != User.Role.ADMIN) {
                JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Only shelters can manage pet listings.");
                return;
            }
        } else if (path.startsWith("/api/applications/submit")) {
            if (currentUser.getRole() != User.Role.ADOPTER) {
                JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Only adopters can submit adoption applications.");
                return;
            }
        } else if (path.startsWith("/api/applications/review")) {
            if (currentUser.getRole() != User.Role.SHELTER && currentUser.getRole() != User.Role.ADMIN) {
                JsonUtil.sendError(res, HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Only shelters can review adoption applications.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicRoute(String path, String method) {
        if (path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/me")) {
            return true;
        }

        // Public pet browsing is accessible without authentication
        if (path.startsWith("/api/pets/public") || path.startsWith("/api/pets/detail")) {
            return true;
        }

        return false;
    }
}
