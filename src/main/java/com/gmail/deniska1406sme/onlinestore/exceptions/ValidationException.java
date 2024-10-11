package com.gmail.deniska1406sme.onlinestore.exceptions;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> validationErrors;

    public ValidationException(List<String> validationErrors) {
        super("Validation failed");
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
