package com.petadoption.util;

import com.petadoption.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecurityUtil {

    public static final String SESSION_USER_KEY = "CURRENT_USER";
    public static final String SESSION_CSRF_KEY = "CSRF_TOKEN";
    public static final String HEADER_CSRF_TOKEN = "X-CSRF-Token";

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtil() {}

    public static String generateCsrfToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object userObj = session.getAttribute(SESSION_USER_KEY);
        if (userObj instanceof User) {
            return (User) userObj;
        }
        return null;
    }

    public static void setAuthenticatedUser(HttpServletRequest request, User user) {
        // Change session ID upon authentication to prevent session fixation attacks if session already exists
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, user);

        // Assign a new CSRF token for the authenticated session
        String csrfToken = generateCsrfToken();
        session.setAttribute(SESSION_CSRF_KEY, csrfToken);
    }

    public static String getOrCreateCsrfToken(HttpSession session) {
        if (session == null) return null;
        String token = (String) session.getAttribute(SESSION_CSRF_KEY);
        if (token == null) {
            token = generateCsrfToken();
            session.setAttribute(SESSION_CSRF_KEY, token);
        }
        return token;
    }

    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
