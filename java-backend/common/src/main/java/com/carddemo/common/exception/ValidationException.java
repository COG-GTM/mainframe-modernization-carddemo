package com.carddemo.common.exception;

/**
 * Exception thrown when input validation fails.
 * Maps to validation checks in COBOL programs like:
 *   - 'Please enter User ID ...'
 *   - 'Please enter Password ...'
 *   - 'First Name can NOT be empty...'
 *   - 'User Type can NOT be empty...'
 */
public class ValidationException extends CardDemoException {

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String field, String message) {
        super(String.format("Validation failed for field '%s': %s", field, message), "VALIDATION_ERROR");
    }
}
