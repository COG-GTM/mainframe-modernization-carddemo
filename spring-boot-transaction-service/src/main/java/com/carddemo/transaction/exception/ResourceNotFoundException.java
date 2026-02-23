package com.carddemo.transaction.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Replaces COBOL DFHRESP(NOTFND) handling in CICS READ commands.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
