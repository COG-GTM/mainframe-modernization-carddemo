package com.carddemo.exception;

/**
 * Raised when a converted COBOL validation fails. The message is the text the original program
 * displayed on the BMS map, so behaviour stays observable to users of the REST API.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
