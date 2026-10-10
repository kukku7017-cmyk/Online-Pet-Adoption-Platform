package com.petadoption.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.petadoption.exception.AppException;
import com.petadoption.model.Conversation;
import com.petadoption.model.Message;
import com.petadoption.model.User;
import com.petadoption.service.MessageService;
import com.petadoption.service.MessageServiceImpl;
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

@WebServlet("/api/messages/*")
public class MessageServlet extends HttpServlet {

    private final MessageService messageService;

    public MessageServlet() {
        this(new MessageServiceImpl());
    }

    public MessageServlet(MessageService messageService) {
        this.messageService = messageService;
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
                case "/conversations" -> handleGetConversations(req, resp, currentUser);
                case "/thread" -> handleGetThread(req, resp, currentUser);
                case "/unread-count" -> handleGetUnreadCount(req, resp, currentUser);
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
            if ("/send".equals(path)) {
                handleSendMessage(req, resp, currentUser);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (AppException e) {
            JsonUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private void handleGetConversations(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        List<Conversation> list = messageService.getUserConversations(currentUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("conversations", list);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleGetThread(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String idParam = req.getParameter("conversationId");
        if (idParam == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing conversationId parameter");
            return;
        }

        long conversationId;
        try {
            conversationId = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid conversationId");
            return;
        }

        List<Message> messages = messageService.getConversationMessages(conversationId, currentUser);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("messages", messages);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    private void handleSendMessage(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        JsonNode json = JsonUtil.fromReader(req.getReader(), JsonNode.class);
        long conversationId = json.path("conversationId").asLong();
        String content = json.path("content").asText();

        Message created = messageService.sendMessage(conversationId, content, currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", created);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, data);
    }

    private void handleGetUnreadCount(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        long count = messageService.getUnreadCount(currentUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("unreadCount", count);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }
}
