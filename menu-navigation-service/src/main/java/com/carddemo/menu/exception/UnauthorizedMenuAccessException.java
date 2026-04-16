package com.carddemo.menu.exception;

/**
 * Thrown when a regular user attempts to access an admin-only menu option.
 * Corresponds to the COBOL error message:
 * "No access - Admin Only option..."
 */
public class UnauthorizedMenuAccessException extends RuntimeException {

    public UnauthorizedMenuAccessException(String message) {
        super(message);
    }
}
