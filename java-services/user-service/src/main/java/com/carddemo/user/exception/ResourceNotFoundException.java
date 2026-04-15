package com.carddemo.user.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to the COBOL DFHRESP(NOTFND) condition in COUSR02C/COUSR03C
 * which displays 'User ID NOT found...' message.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
