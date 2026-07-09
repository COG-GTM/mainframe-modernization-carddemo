package com.carddemo.web.transaction.dto;

/**
 * Error body for the transaction endpoints, carrying the verbatim COBOL screen message
 * (the {@code WS-MESSAGE} value the online programs place in {@code ERRMSGO}).
 *
 * @param message the COBOL validation / not-found message
 */
public record TransactionErrorResponse(String message) {
}
