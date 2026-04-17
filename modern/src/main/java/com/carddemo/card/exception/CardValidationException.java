package com.carddemo.card.exception;

import java.util.List;

/**
 * Thrown when card update validation fails.
 *
 * Migrated from: COCRDUPC.cbl validation logic
 * COBOL pattern: sets WS-ERR-FLG to 'Y', moves error message to screen,
 * positions cursor at invalid field, then SEND MAP.
 */
public class CardValidationException extends RuntimeException {

    private final List<String> errors;

    public CardValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public CardValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
