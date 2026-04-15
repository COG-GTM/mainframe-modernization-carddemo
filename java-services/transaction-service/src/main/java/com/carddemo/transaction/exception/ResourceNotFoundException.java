package com.carddemo.transaction.exception;

/**
 * Mirrors COBOL DFHRESP(NOTFND) handling in COTRN01C READ-TRANSACT-FILE:
 * "Transaction ID NOT found..."
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
