package com.carddemo.common.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Maps to COBOL response codes DUPKEY/DUPREC in the original programs.
 * 
 * Example from COUSR01C.cbl:
 *   WHEN DFHRESP(DUPKEY)
 *   WHEN DFHRESP(DUPREC)
 *       MOVE 'User ID already exist...' TO WS-MESSAGE
 */
public class DuplicateResourceException extends CardDemoException {

    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s already exists with identifier: %s", resourceType, identifier), "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}
