package com.carddemo.transaction.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * COBOL Traceability: Replaces the DFHRESP(NOTFND) response code handling
 * in CICS READ operations across COTRN01C, COTRN02C, and COBIL00C.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
