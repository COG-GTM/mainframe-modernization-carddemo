package com.carddemo.user.exception;

/**
 * Exception thrown when a user is not found by their ID.
 * <p>
 * Migrated from: CICS RESP code DFHRESP(NOTFND) on READ operations
 * Maps to HTTP 404 Not Found.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}
