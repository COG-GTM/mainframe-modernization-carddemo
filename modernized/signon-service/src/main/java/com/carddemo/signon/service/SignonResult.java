package com.carddemo.signon.service;

import com.carddemo.signon.domain.UserType;

/**
 * Outcome of a signon attempt. Each {@link Outcome} value corresponds to one
 * branch of {@code COSGN00C} ({@code PROCESS-ENTER-KEY} / {@code READ-USER-SEC-FILE}).
 *
 * @param outcome            which mainframe branch was taken
 * @param message            error message shown to the user (empty on success)
 * @param destinationProgram XCTL navigation target on success ({@code COADM01C}
 *                           or {@code COMEN01C}); {@code null} on failure
 * @param userId             normalized (upper-cased) user id that was evaluated
 * @param userType           resolved user type on success; {@code null} otherwise
 */
public record SignonResult(
        Outcome outcome,
        String message,
        String destinationProgram,
        String userId,
        UserType userType) {

    public enum Outcome {
        /** Valid admin credentials → route to admin menu (COADM01C). */
        ADMIN_MENU,
        /** Valid regular credentials → route to main menu (COMEN01C). */
        MAIN_MENU,
        /** User id was blank. */
        MISSING_USER_ID,
        /** Password was blank. */
        MISSING_PASSWORD,
        /** User exists but password did not match. */
        WRONG_PASSWORD,
        /** User id not present in USRSEC (CICS resp 13, NOTFND). */
        USER_NOT_FOUND,
        /** Datastore lookup failed for any other reason (CICS resp OTHER). */
        VERIFY_ERROR
    }

    /** @return true when the attempt succeeded and routing is available. */
    public boolean isSuccess() {
        return outcome == Outcome.ADMIN_MENU || outcome == Outcome.MAIN_MENU;
    }

    static SignonResult success(Outcome outcome, String destinationProgram,
                                String userId, UserType userType) {
        return new SignonResult(outcome, "", destinationProgram, userId, userType);
    }

    static SignonResult failure(Outcome outcome, String message, String userId) {
        return new SignonResult(outcome, message, null, userId, null);
    }
}
