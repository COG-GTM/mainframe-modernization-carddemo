package com.carddemo.security;

import org.springframework.http.HttpStatus;

/**
 * Raised when the sign-on flow rejects a request, carrying the verbatim {@code COSGN00C}
 * message (see {@link SignonMessages}) and the HTTP status the controller should return.
 */
public class SignonException extends RuntimeException {

    private final transient HttpStatus status;

    public SignonException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
