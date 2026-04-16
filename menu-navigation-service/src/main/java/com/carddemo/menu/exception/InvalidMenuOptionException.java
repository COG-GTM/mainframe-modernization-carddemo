package com.carddemo.menu.exception;

/**
 * Thrown when a user selects an invalid menu option.
 * Corresponds to the COBOL error message:
 * "Please enter a valid option number..."
 */
public class InvalidMenuOptionException extends RuntimeException {

    public InvalidMenuOptionException(String message) {
        super(message);
    }
}
