package com.petadoption.exception;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
