package com.petadoption.controller;

import com.petadoption.model.User;
import com.petadoption.util.JsonUtil;
import com.petadoption.util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@WebServlet("/api/upload")
@MultipartConfig(
    maxFileSize = 5242880,       // 5MB
    maxRequestSize = 10485760,   // 10MB
    fileSizeThreshold = 1048576  // 1MB
)
public class UploadServlet extends HttpServlet {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SecurityUtil.getAuthenticatedUser(req);
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required to upload files.");
            return;
        }

        if (currentUser.getRole() != User.Role.SHELTER && currentUser.getRole() != User.Role.ADMIN) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Only shelters and administrators can upload pet photos.");
            return;
        }

        Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "No file provided in form upload.");
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid file format. Allowed formats: JPEG, PNG, WEBP.");
            return;
        }

        String submittedFileName = filePart.getSubmittedFileName();
        String extension = ".jpg";
        if (submittedFileName != null && submittedFileName.contains(".")) {
            extension = submittedFileName.substring(submittedFileName.lastIndexOf(".")).toLowerCase();
            if (!Set.of(".jpg", ".jpeg", ".png", ".webp").contains(extension)) {
                extension = ".jpg";
            }
        }

        String safeFileName = UUID.randomUUID().toString() + extension;

        // Destination: uploads directory in user workspace or servlet context
        String uploadDirPath = getServletContext().getRealPath("/assets/uploads");
        if (uploadDirPath == null) {
            uploadDirPath = System.getProperty("user.dir") + File.separator + "uploads";
        }

        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        Path targetPath = Paths.get(uploadDirPath, safeFileName);
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String publicUrl = "assets/uploads/" + safeFileName;
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("url", publicUrl);
        data.put("fileName", safeFileName);

        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }
}
