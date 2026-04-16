package com.carddemo.transaction.exception;

/**
 * Thrown when a transaction is not found by ID.
 * Preserves COBOL error: 'Transaction ID NOT found...'
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String tranId) {
        super("Transaction ID NOT found...");
    }
}
