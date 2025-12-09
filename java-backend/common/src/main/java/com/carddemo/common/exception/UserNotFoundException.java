package com.carddemo.common.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CardDemoException {
    public UserNotFoundException(String userId) {
        super("User not found: " + userId, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }
}
