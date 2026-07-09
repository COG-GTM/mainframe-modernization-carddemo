package com.carddemo.service.billpay;

import org.springframework.http.HttpStatus;

/**
 * Raised when the bill-payment flow rejects a request, carrying the verbatim
 * {@code COBIL00C} message (see {@link BillPayMessages}) and the HTTP status the controller
 * should return. Mirrors the {@code MOVE 'Y' TO WS-ERR-FLG} error branches that re-display
 * the screen with an {@code ERRMSGO} message.
 */
public class BillPayException extends RuntimeException {

    private final transient HttpStatus status;

    public BillPayException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
