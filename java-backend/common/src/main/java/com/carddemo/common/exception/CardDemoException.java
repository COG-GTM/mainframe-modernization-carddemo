package com.carddemo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CardDemoException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public CardDemoException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "CARDDEMO_ERROR";
    }

    public CardDemoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "CARDDEMO_ERROR";
    }

    public CardDemoException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
