package com.boardly.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldsValidationException extends ApiException {

    private final Map<String, List<String>> errors = new HashMap<>();

    public FieldsValidationException() {
        super("Field validation failed");
    }

    public FieldsValidationException(String message) {
        super(message);
    }

    public void addError(String field, String errorMessage) {
        errors.computeIfAbsent(field, k -> new ArrayList<>())
                .add(errorMessage);
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
