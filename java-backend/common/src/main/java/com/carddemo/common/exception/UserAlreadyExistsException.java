package com.carddemo.common.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends CardDemoException {
    public UserAlreadyExistsException(String userId) {
        super("User already exists: " + userId, HttpStatus.CONFLICT, "USER_EXISTS");
    }
}
