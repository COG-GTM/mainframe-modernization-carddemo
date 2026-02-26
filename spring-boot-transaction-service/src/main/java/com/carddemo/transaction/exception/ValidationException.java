package com.carddemo.transaction.exception;

/**
 * Exception thrown when business validation fails.
 * Replaces the COBOL WS-ERR-FLG / WS-MESSAGE pattern
 * used in VALIDATE-INPUT-KEY-FIELDS and VALIDATE-INPUT-DATA-FIELDS.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
