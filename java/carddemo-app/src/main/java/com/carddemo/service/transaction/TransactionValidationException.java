package com.carddemo.service.transaction;

/**
 * Raised when the transaction-add flow rejects input, carrying the verbatim {@code COTRN02C}
 * (and {@code COTRN00C}/{@code COTRN01C}) validation message. Maps to HTTP 400.
 *
 * <p>The COBOL programs set {@code WS-ERR-FLG} and re-display the map with the message; here
 * the equivalent is a checked-at-the-boundary runtime exception the controller/handler turns
 * into a {@code 400 Bad Request} response (or a re-displayed screen for the nav handler).</p>
 */
public class TransactionValidationException extends RuntimeException {

    public TransactionValidationException(String message) {
        super(message);
    }
}
