package com.carddemo.transaction.exception;

/**
 * Thrown when an account ID lookup fails in the cross-reference table.
 *
 * Replaces COBOL DFHRESP(NOTFND) handling in READ-CXACAIX-FILE:
 *   "Account ID NOT found..."
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId) {
        super("Account ID NOT found: " + accountId);
    }
}
