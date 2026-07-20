package com.carddemo.signon.web;

/**
 * Signon request payload. Corresponds to the {@code USERID}/{@code PASSWD}
 * fields of BMS map {@code COSGN0A} that {@code COSGN00C} reads via
 * {@code EXEC CICS RECEIVE MAP}.
 */
public record SignonRequest(String userId, String password) {
}
