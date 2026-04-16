package com.carddemo.accountservice.exception;

/**
 * Thrown when account is not found in account master file.
 * Preserves COBOL error message from COACTVWC.cbl / COACTUPC.cbl.
 */
public class AccountNotFoundException extends RuntimeException {

    public static final String ACCT_NOT_FOUND_IN_MASTER =
            "Did not find this account in account master file";

    public AccountNotFoundException() {
        super(ACCT_NOT_FOUND_IN_MASTER);
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}
