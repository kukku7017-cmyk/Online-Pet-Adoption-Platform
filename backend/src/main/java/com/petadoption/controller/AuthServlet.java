package com.petadoption.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petadoption.exception.AppException;
import com.petadoption.model.User;
import com.petadoption.service.UserService;
import com.petadoption.service.UserServiceImpl;
import com.petadoption.util.JsonUtil;
import com.petadoption.util.SecurityUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final UserService userService;

    public AuthServlet() {
        this(new UserServiceImpl());
    }

    public AuthServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        if (path.equals("/me")) {
            handleGetMe(req, resp);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/login" -> handleLogin(req, resp);
                case "/register" -> handleRegister(req, resp);
                case "/logout" -> handleLogout(req, resp);
                case "/profile" -> handleUpdateProfile(req, resp);
                default -> JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found");
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        String email = json.path("email").asText();
        String password = json.path("password").asText();

        User user = userService.authenticate(email, password);

        // Secure session setup with session fixation defense
        SecurityUtil.setAuthenticatedUser(req, user);

        HttpSession session = req.getSession(false);
        String csrfToken = (String) session.getAttribute(SecurityUtil.SESSION_CSRF_KEY);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Login successful");
        responseData.put("user", user);
        responseData.put("csrfToken", csrfToken);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, responseData);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        String email = json.path("email").asText();
        String password = json.path("password").asText();
        String fullName = json.path("fullName").asText();
        String phone = json.has("phone") ? json.path("phone").asText() : null;
        String address = json.has("address") ? json.path("address").asText() : null;
        String roleStr = json.path("role").asText();

        User created = userService.register(email, password, fullName, phone, address, roleStr);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Registration successful! You may now sign in.");
        responseData.put("userId", created.getId());

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, responseData);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SecurityUtil.invalidateSession(req);
        JsonUtil.sendSuccess(resp, "Successfully logged out.");
    }

    private void handleGetMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = SecurityUtil.getAuthenticatedUser(req);
        Map<String, Object> responseData = new HashMap<>();

        if (user != null) {
            HttpSession session = req.getSession(false);
            String csrfToken = SecurityUtil.getOrCreateCsrfToken(session);

            responseData.put("authenticated", true);
            responseData.put("user", user);
            responseData.put("csrfToken", csrfToken);
        } else {
            responseData.put("authenticated", false);
        }

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, responseData);
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = SecurityUtil.getAuthenticatedUser(req);
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please sign in to update profile.");
            return;
        }

        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        String fullName = json.path("fullName").asText();
        String phone = json.has("phone") ? json.path("phone").asText() : null;
        String address = json.has("address") ? json.path("address").asText() : null;

        User updated = userService.updateProfile(currentUser.getId(), fullName, phone, address);
        req.getSession().setAttribute(SecurityUtil.SESSION_USER_KEY, updated);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Profile updated successfully.");
        responseData.put("user", updated);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, responseData);
    }
}
