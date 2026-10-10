package com.petadoption.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtil() {}

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T fromReader(Reader reader, Class<T> clazz) {
        try {
            return MAPPER.readValue(reader, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON payload into " + clazz.getSimpleName(), e);
        }
    }

    public static void sendJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(toJson(data));
        response.getWriter().flush();
    }

    public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
        sendJsonResponse(response, HttpServletResponse.SC_OK, Map.of(
            "success", true,
            "message", message
        ));
    }

    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        sendJsonResponse(response, statusCode, Map.of(
            "success", false,
            "message", message != null ? message : "An error occurred"
        ));
    }
}
