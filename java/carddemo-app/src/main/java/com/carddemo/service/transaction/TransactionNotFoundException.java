package com.carddemo.service.transaction;

/**
 * Raised when a transaction lookup finds no record — the Java equivalent of the
 * {@code COTRN01C} {@code DFHRESP(NOTFND)} branch ("Transaction ID NOT found..."). Maps to
 * HTTP 404.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String message) {
        super(message);
    }
}
