package com.carddemo.accountservice.exception;

/**
 * Thrown when associated customer is not found in customer master file.
 * Preserves COBOL error message from COACTVWC.cbl.
 */
public class CustomerNotFoundException extends RuntimeException {

    public static final String CUST_NOT_FOUND_IN_MASTER =
            "Did not find associated customer in master file";

    public CustomerNotFoundException() {
        super(CUST_NOT_FOUND_IN_MASTER);
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
