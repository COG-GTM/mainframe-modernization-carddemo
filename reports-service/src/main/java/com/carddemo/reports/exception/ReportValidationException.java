package com.carddemo.reports.exception;

import java.util.List;

public class ReportValidationException extends RuntimeException {

    private final List<String> errors;

    public ReportValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public ReportValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
