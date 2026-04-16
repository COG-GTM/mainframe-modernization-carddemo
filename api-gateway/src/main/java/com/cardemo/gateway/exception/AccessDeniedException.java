package com.cardemo.gateway.exception;

/**
 * Thrown when a user attempts to access a route that requires a higher privilege level.
 * Maps the COBOL error: "No access - Admin Only option..."
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
