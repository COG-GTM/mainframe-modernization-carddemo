package com.carddemo.service.report;

import org.springframework.http.HttpStatus;

/**
 * Raised when the transaction-report request flow rejects a request, carrying the verbatim
 * {@code CORPT00C} message (see {@link ReportMessages}) and the HTTP status the controller
 * should return. Mirrors the {@code MOVE 'Y' TO WS-ERR-FLG} branches that re-display
 * {@code CORPT0A} with an {@code ERRMSGO} message.
 */
public class ReportException extends RuntimeException {

    private final transient HttpStatus status;

    public ReportException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
