package com.carddemo.util;

/**
 * Runtime exception replacing CEE3ABD calls from COBOL.
 * Triggers application error handling via @ControllerAdvice.
 */
public class ApplicationAbendException extends RuntimeException {

    private final int abendCode;

    public ApplicationAbendException(int abendCode, String message) {
        super(message);
        this.abendCode = abendCode;
    }

    public ApplicationAbendException(int abendCode, String message, Throwable cause) {
        super(message, cause);
        this.abendCode = abendCode;
    }

    public int getAbendCode() {
        return abendCode;
    }
}
