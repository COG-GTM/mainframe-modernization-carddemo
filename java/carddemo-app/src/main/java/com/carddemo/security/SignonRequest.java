package com.carddemo.security;

/**
 * Sign-on request payload for {@code POST /api/auth/signon}.
 *
 * <p>Mirrors the two inputs of the {@code COSGN0A} map ({@code USERIDI} / {@code PASSWDI}).
 * Both are validated and upper-cased by {@link AuthService}, reproducing
 * {@code MOVE FUNCTION UPPER-CASE(...)} in {@code COSGN00C}.</p>
 *
 * @param userId   USERIDI — SEC-USR-ID PIC X(08)
 * @param password PASSWDI — SEC-USR-PWD PIC X(08)
 */
public record SignonRequest(String userId, String password) {
}
