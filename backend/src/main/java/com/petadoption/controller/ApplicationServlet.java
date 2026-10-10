package com.petadoption.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petadoption.exception.AppException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.AdoptionServiceImpl;
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

@WebServlet("/api/applications/*")
public class ApplicationServlet extends HttpServlet {

    private final AdoptionService adoptionService;

    public ApplicationServlet() {
        this(new AdoptionServiceImpl());
    }

    public ApplicationServlet(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        User currentUser = SecurityUtil.getAuthenticatedUser(req);
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        try {
            switch (path) {
                case "/my" -> handleGetMyApplications(req, resp, currentUser);
                case "/shelter" -> handleGetShelterApplications(req, resp, currentUser);
                case "/detail" -> handleGetDetail(req, resp, currentUser);
                default -> JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
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
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        try {
            switch (path) {
                case "/submit" -> handleSubmit(req, resp, currentUser);
                case "/review" -> handleReview(req, resp, currentUser);
                default -> JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private void handleSubmit(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        AdoptionApplication app = JsonUtil.fromReader(req.getReader(), AdoptionApplication.class);
        AdoptionApplication created = adoptionService.submitApplication(app, currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Adoption application submitted successfully.");
        data.put("applicationId", created.getId());

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, data);
    }

    private void handleReview(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        long applicationId = json.path("applicationId").asLong();
        String statusStr = json.path("status").asText();
        String reviewNotes = json.has("reviewNotes") ? json.path("reviewNotes").asText() : null;

        adoptionService.reviewApplication(applicationId, statusStr, reviewNotes, currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Application reviewed and status updated successfully.");

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetMyApplications(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        List<AdoptionApplication> apps = adoptionService.getAdopterApplications(currentUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("applications", apps);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetShelterApplications(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        List<AdoptionApplication> apps = adoptionService.getShelterApplications(currentUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("applications", apps);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetDetail(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing application ID parameter");
            return;
        }

        long appId;
        try {
            appId = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid application ID");
            return;
        }

        AdoptionApplication app = adoptionService.getApplicationById(appId, currentUser);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("application", app);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }
}
