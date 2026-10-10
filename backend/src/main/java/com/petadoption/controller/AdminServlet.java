package com.petadoption.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petadoption.dao.AuditLogDAO;
import com.petadoption.dao.AuditLogDAOImpl;
import com.petadoption.exception.AppException;
import com.petadoption.model.AuditLog;
import com.petadoption.model.Pet;
import com.petadoption.model.PlatformAnalytics;
import com.petadoption.model.SystemSetting;
import com.petadoption.model.User;
import com.petadoption.service.*;
import com.petadoption.util.JsonUtil;
import com.petadoption.util.SecurityUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final UserService userService;
    private final PetService petService;
    private final AnalyticsService analyticsService;
    private final SettingService settingService;
    private final AuditLogDAO auditLogDAO;

    public AdminServlet() {
        this(new UserServiceImpl(), new PetServiceImpl(), new AnalyticsServiceImpl(), new SettingService(), new AuditLogDAOImpl());
    }

    public AdminServlet(UserService userService, PetService petService, AnalyticsService analyticsService,
                        SettingService settingService, AuditLogDAO auditLogDAO) {
        this.userService = userService;
        this.petService = petService;
        this.analyticsService = analyticsService;
        this.settingService = settingService;
        this.auditLogDAO = auditLogDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/users" -> handleGetUsers(req, resp);
                case "/pets/pending" -> handleGetPendingPets(req, resp);
                case "/analytics" -> handleGetAnalytics(req, resp);
                case "/settings" -> handleGetSettings(req, resp);
                case "/logs" -> handleGetAuditLogs(req, resp);
                default -> JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Admin endpoint not found");
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        User currentUser = SecurityUtil.getAuthenticatedUser(req);

        try {
            switch (path) {
                case "/user-status" -> handleUpdateUserStatus(req, resp, currentUser);
                case "/pets/moderate" -> handleModeratePet(req, resp, currentUser);
                case "/settings" -> handleUpdateSetting(req, resp);
                default -> JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Admin endpoint not found");
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private void handleGetUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = req.getParameter("role");
        List<User> users = (role != null && !role.isBlank()) ? userService.getUsersByRole(role) : userService.getAllUsers();

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("users", users);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleUpdateUserStatus(HttpServletRequest req, HttpServletResponse resp, User adminUser) throws IOException {
        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        long targetUserId = json.path("userId").asLong();
        String statusStr = json.path("status").asText();

        userService.updateUserStatus(adminUser.getId(), targetUserId, statusStr);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "User account status updated successfully.");

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetPendingPets(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Pet> pets = petService.getPendingPets();
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("pets", pets);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleModeratePet(HttpServletRequest req, HttpServletResponse resp, User adminUser) throws IOException {
        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        long petId = json.path("petId").asLong();
        String statusStr = json.path("status").asText();
        String rejectionReason = json.has("rejectionReason") ? json.path("rejectionReason").asText() : null;

        petService.moderatePet(petId, statusStr, rejectionReason, adminUser);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Pet listing moderation recorded successfully.");

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetAnalytics(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PlatformAnalytics analytics = analyticsService.getPlatformAnalytics();
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("analytics", analytics);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetSettings(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<SystemSetting> settings = settingService.getAllSettings();
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("settings", settings);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleUpdateSetting(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        String key = json.path("key").asText();
        String value = json.path("value").asText();

        settingService.updateSetting(key, value);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "System setting updated successfully.");

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetAuditLogs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int limit = 50;
        try {
            if (req.getParameter("limit") != null) limit = Integer.parseInt(req.getParameter("limit"));
        } catch (NumberFormatException ignored) {}

        List<AuditLog> logs = auditLogDAO.findRecent(limit);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("logs", logs);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }
}
