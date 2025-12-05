package com.carddemo.common.exception;

/**
 * Exception thrown when authentication fails.
 * Maps to authentication failures in COSGN00C.cbl:
 *   - 'Wrong Password. Try again ...'
 *   - 'User not found. Try again ...'
 *   - 'Unable to verify the User ...'
 */
public class AuthenticationException extends CardDemoException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", cause);
    }
}
