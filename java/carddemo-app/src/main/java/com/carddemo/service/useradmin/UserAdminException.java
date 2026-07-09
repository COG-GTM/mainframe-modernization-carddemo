package com.carddemo.service.useradmin;

import org.springframework.http.HttpStatus;

/**
 * Raised when a user-administration operation is rejected, carrying the verbatim
 * {@code COUSRxxC} message (see {@link UserAdminMessages}) and the HTTP status the controller
 * should return: validation failures → {@code 400}, duplicate id (add) → {@code 409},
 * missing id (update/delete) → {@code 404}.
 */
public class UserAdminException extends RuntimeException {

    private final transient HttpStatus status;

    public UserAdminException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
