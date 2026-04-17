package com.carddemo.auth.exception;

/**
 * Exception thrown when authentication fails.
 *
 * Migrated from: COSGN00C.cbl, READ-USER-SEC-FILE paragraph (lines 209-257)
 * Maps to the COBOL error messages:
 *   - "Wrong Password. Try again ..." (RESP=0 but password mismatch)
 *   - "User not found. Try again ..." (RESP=13, DFHRESP(NOTFND))
 *   - "Unable to verify the User ..." (RESP=OTHER)
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
