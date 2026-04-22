package com.carddemo.transaction.exception;

/**
 * Mirrors COBOL validation error handling in COTRN02C VALIDATE-INPUT-KEY-FIELDS
 * and VALIDATE-INPUT-DATA-FIELDS, e.g.:
 * "Card Number must be Numeric...", "Type CD can NOT be empty...", "Amount can NOT be empty..."
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
