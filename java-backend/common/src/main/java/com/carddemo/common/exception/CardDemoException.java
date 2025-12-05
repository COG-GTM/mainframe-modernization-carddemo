package com.carddemo.common.exception;

import lombok.Getter;

/**
 * Base exception class for all CardDemo application exceptions.
 * Provides consistent error handling across all services.
 */
@Getter
public class CardDemoException extends RuntimeException {

    private final String errorCode;

    public CardDemoException(String message) {
        super(message);
        this.errorCode = "CARDDEMO_ERROR";
    }

    public CardDemoException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public CardDemoException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CARDDEMO_ERROR";
    }

    public CardDemoException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
