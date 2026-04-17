package com.carddemo.account.exception;

/**
 * Thrown when a requested resource (account, customer) is not found.
 * Migrated from: CICS NOTFND condition handling in COACTVWC.cbl / COACTUPC.cbl.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
