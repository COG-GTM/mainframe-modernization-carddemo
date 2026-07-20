package com.carddemo.signon.service;

/**
 * User-facing messages and navigation targets, copied verbatim from
 * {@code COSGN00C} so the modernized service is behaviourally identical to the
 * mainframe program. These strings are the functional-parity contract asserted
 * by the test suite.
 */
public final class SignonMessages {

    /** {@code WHEN USERIDI ... = SPACES OR LOW-VALUES} branch. */
    public static final String ENTER_USER_ID = "Please enter User ID ...";
    /** {@code WHEN PASSWDI ... = SPACES OR LOW-VALUES} branch. */
    public static final String ENTER_PASSWORD = "Please enter Password ...";
    /** {@code READ} resp 0, password mismatch branch. */
    public static final String WRONG_PASSWORD = "Wrong Password. Try again ...";
    /** {@code READ} resp 13 (NOTFND) branch. */
    public static final String USER_NOT_FOUND = "User not found. Try again ...";
    /** {@code READ} resp OTHER branch. */
    public static final String VERIFY_ERROR = "Unable to verify the User ...";

    /** {@code EXEC CICS XCTL PROGRAM('COADM01C')} — admin menu. */
    public static final String ADMIN_PROGRAM = "COADM01C";
    /** {@code EXEC CICS XCTL PROGRAM('COMEN01C')} — main (back-office) menu. */
    public static final String MAIN_PROGRAM = "COMEN01C";

    private SignonMessages() {
    }
}
