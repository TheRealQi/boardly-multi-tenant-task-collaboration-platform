package com.boardly.exception;

import java.util.HashMap;
import java.util.Map;

public class FieldsValidationException extends ApiException {

    private final Map<String, String> errors = new HashMap<>();

    public FieldsValidationException() {
        super("Field validation failed");
    }

    public FieldsValidationException(String message) {
        super(message);
    }

    public void addError(String field, String errorMessage) {
        errors.put(field, errorMessage);
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}