package com.carddemo.transaction.exception;

/**
 * Exception thrown for invalid business rule violations.
 *
 * COBOL Traceability: Replaces the WS-ERR-FLG / WS-MESSAGE pattern used
 * across COTRN02C and COBIL00C for validation error reporting.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
