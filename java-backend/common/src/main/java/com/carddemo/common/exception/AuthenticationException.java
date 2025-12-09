package com.carddemo.common.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends CardDemoException {
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTH_ERROR");
    }
}
