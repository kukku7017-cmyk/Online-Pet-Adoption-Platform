package com.petadoption.util;

import com.petadoption.exception.ValidationException;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s\\-\\(\\)]{7,20}$"
    );

    private ValidationUtil() {}

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }

    public static void validateEmail(String email) {
        requireNonBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validatePasswordStrength(String password) {
        requireNonBlank(password, "Password");
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Invalid phone number format.");
        }
    }

    public static String sanitize(String input) {
        if (input == null) return null;
        return input.trim()
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
