package com.carddemo.transaction.exception;

/**
 * Thrown when a requested transaction is not found.
 *
 * Replaces COBOL DFHRESP(NOTFND) handling in STARTBR-TRANSACT-FILE:
 *   "Transaction ID NOT found..."
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long transactionId) {
        super("Transaction ID NOT found: " + transactionId);
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }
}
