package com.carddemo.cardservice.exception;

/**
 * Thrown for invalid action codes.
 * Preserves COBOL message: "INVALID ACTION CODE".
 */
public class InvalidActionException extends RuntimeException {

    public InvalidActionException(String message) {
        super(message);
    }

    public InvalidActionException() {
        super("INVALID ACTION CODE");
    }
}
