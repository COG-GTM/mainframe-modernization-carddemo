package com.carddemo.accountservice.exception;

import java.util.List;

/**
 * Thrown when account/customer update validation fails.
 * Carries all validation error messages preserving COBOL messages.
 */
public class AccountValidationException extends RuntimeException {

    private final List<String> errors;

    public AccountValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public AccountValidationException(List<String> errors) {
        super(errors.isEmpty() ? "Validation failed" : errors.get(0));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
