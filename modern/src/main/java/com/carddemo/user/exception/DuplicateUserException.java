package com.carddemo.user.exception;

/**
 * Exception thrown when attempting to create a user with an ID that already exists.
 * <p>
 * Migrated from: CICS RESP code DFHRESP(DUPREC) on WRITE operations
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String userId) {
        super("User already exists: " + userId);
    }
}
