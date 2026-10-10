package com.petadoption.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petadoption.exception.AppException;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.service.PetService;
import com.petadoption.service.PetServiceImpl;
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

@WebServlet("/api/pets/*")
public class PetServlet extends HttpServlet {

    private final PetService petService;

    public PetServlet() {
        this(new PetServiceImpl());
    }

    public PetServlet(PetService petService) {
        this.petService = petService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/public" -> handleSearchPublic(req, resp);
                case "/detail" -> handleGetDetail(req, resp);
                case "/my-listings" -> handleGetMyListings(req, resp);
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

        try {
            switch (path) {
                case "/create" -> handleCreatePet(req, resp);
                case "/update" -> handleUpdatePet(req, resp);
                default -> JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private void handleSearchPublic(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String species = req.getParameter("species");
        String breed = req.getParameter("breed");
        String gender = req.getParameter("gender");
        String size = req.getParameter("size");
        String query = req.getParameter("query");

        int page = 1;
        int limit = 9;
        try {
            if (req.getParameter("page") != null) page = Integer.parseInt(req.getParameter("page"));
            if (req.getParameter("limit") != null) limit = Integer.parseInt(req.getParameter("limit"));
        } catch (NumberFormatException ignored) {}

        Map<String, Object> result = petService.searchPublicPets(species, breed, gender, size, query, page, limit);
        result.put("success", true);
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleGetDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing pet ID parameter");
            return;
        }

        long petId;
        try {
            petId = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid pet ID parameter");
            return;
        }

        Pet pet = petService.getPetById(petId);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("pet", pet);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetMyListings(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = SecurityUtil.getAuthenticatedUser(req);
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        List<Pet> pets = petService.getShelterPets(currentUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("pets", pets);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleCreatePet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = SecurityUtil.getAuthenticatedUser(req);
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        Pet pet = JsonUtil.fromReader(req.getReader(), Pet.class);
        Pet created = petService.createPet(pet, currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Pet listing submitted successfully and queued for administrator review.");
        data.put("petId", created.getId());

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, data);
    }

    private void handleUpdatePet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = SecurityUtil.getAuthenticatedUser(req);
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        Pet pet = JsonUtil.fromReader(req.getReader(), Pet.class);
        Pet updated = petService.updatePet(pet, currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Pet listing updated successfully.");
        data.put("pet", updated);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }
}
