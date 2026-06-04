package com.importease.proyecto.service;

import java.util.Map;

public class PredamValidationException extends RuntimeException {
    private final Map<String, Object> validationError;

    public PredamValidationException(Map<String, Object> validationError) {
        super((String) validationError.get("message"));
        this.validationError = validationError;
    }

    public Map<String, Object> getValidationError() {
        return validationError;
    }
}


