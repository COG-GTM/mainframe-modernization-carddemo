package com.carddemo.user.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Maps to the COBOL DFHRESP(DUPREC)/DFHRESP(DUPKEY) condition in COUSR01C
 * which displays 'User ID already exist...' message.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
