package com.carddemo.service.account;

/**
 * Signals that a lookup in the card cross-reference, account master or customer master file
 * failed — the Java equivalent of {@code COACTVWC}/{@code COACTUPC} setting one of the
 * {@code DID-NOT-FIND-*} messages and re-displaying the screen. The controller maps it to
 * HTTP 404 carrying the original COBOL message.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
