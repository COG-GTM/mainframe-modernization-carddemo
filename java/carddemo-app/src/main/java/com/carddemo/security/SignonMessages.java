package com.carddemo.security;

/**
 * Verbatim sign-on messages from {@code COSGN00C}, reused by the REST sign-on so the migrated
 * flow returns the exact wording an operator saw on the 3270 screen ({@code ERRMSGO}).
 */
public final class SignonMessages {

    /** {@code WHEN USERIDI = SPACES OR LOW-VALUES} branch. */
    public static final String EMPTY_USER_ID = "Please enter User ID ...";

    /** {@code WHEN PASSWDI = SPACES OR LOW-VALUES} branch. */
    public static final String EMPTY_PASSWORD = "Please enter Password ...";

    /** {@code READ-USER-SEC-FILE} {@code WHEN 13} (record not found) branch. */
    public static final String USER_NOT_FOUND = "User not found. Try again ...";

    /** {@code IF SEC-USR-PWD = WS-USER-PWD} false branch. */
    public static final String WRONG_PASSWORD = "Wrong Password. Try again ...";

    /** {@code READ-USER-SEC-FILE} {@code WHEN OTHER} (unexpected RESP) branch. */
    public static final String UNABLE_TO_VERIFY = "Unable to verify the User ...";

    private SignonMessages() {
    }
}
