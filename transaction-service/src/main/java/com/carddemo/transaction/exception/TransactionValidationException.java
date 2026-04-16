package com.carddemo.transaction.exception;

/**
 * Thrown when transaction input validation fails.
 * Message text is preserved character-for-character from COBOL source.
 */
public class TransactionValidationException extends RuntimeException {

    public TransactionValidationException(String message) {
        super(message);
    }
}
