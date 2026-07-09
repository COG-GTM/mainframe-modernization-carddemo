package com.carddemo.security;

/**
 * Error response body for a rejected sign-on. {@code message} is the verbatim {@code COSGN00C}
 * screen message (see {@link SignonMessages}).
 *
 * @param message the COBOL-equivalent error message
 */
public record SignonErrorResponse(String message) {
}
