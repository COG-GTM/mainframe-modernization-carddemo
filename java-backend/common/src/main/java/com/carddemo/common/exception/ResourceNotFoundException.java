package com.carddemo.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to COBOL response code 13 (NOTFND) in the original programs.
 * 
 * Example from COSGN00C.cbl:
 *   WHEN 13
 *       MOVE 'User not found. Try again ...' TO WS-MESSAGE
 */
public class ResourceNotFoundException extends CardDemoException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier), "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
