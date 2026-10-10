package com.petadoption.exception;

public class AuthorizationException extends AppException {
    public AuthorizationException(String message) {
        super(message, 403);
    }
}
