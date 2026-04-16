package com.carddemo.transaction.exception;

/**
 * Thrown when an account is not found.
 * Preserves COBOL error: 'Account ID NOT found...'
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("Account ID NOT found...");
    }
}
