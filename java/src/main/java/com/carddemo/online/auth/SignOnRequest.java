package com.carddemo.online.auth;

/**
 * BMS map COSGN0A (mapset COSGN00) input fields used by COBOL program COSGN00C.
 *
 * @param userId USERIDI PIC X(08)
 * @param password PASSWDI PIC X(08)
 */
public record SignOnRequest(String userId, String password) {
}
