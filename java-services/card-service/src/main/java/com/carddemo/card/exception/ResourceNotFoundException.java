package com.carddemo.card.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Corresponds to COBOL conditions like DID-NOT-FIND-ACCTCARD-COMBO
 * and DID-NOT-FIND-ACCT-IN-CARDXREF from COCRDSLC.cbl / COCRDUPC.cbl.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
