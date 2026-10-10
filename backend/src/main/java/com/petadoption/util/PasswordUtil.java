package com.petadoption.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {}

    /**
     * Hashes a plaintext password using BCrypt with salt rounds = 12.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank for hashing");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a candidate plaintext password against a stored BCrypt hash.
     */
    public static boolean checkPassword(String candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidatePassword, storedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
