package com.carddemo.service.useradmin;

/**
 * Verbatim operator messages from the user-maintenance programs {@code COUSR01C} /
 * {@code COUSR02C} / {@code COUSR03C}, reused by the REST layer so the migrated flow returns
 * the exact wording shown on the 3270 screen ({@code ERRMSGO}).
 *
 * <p>The three {@code has been added/updated/deleted} builders reproduce the COBOL
 * {@code STRING 'User ' DELIMITED BY SIZE, SEC-USR-ID DELIMITED BY SPACE, ' has been ... '}
 * (the id delimited by space = trailing blanks trimmed).</p>
 *
 * <p>A few validations have no direct {@code COUSRxxC} equivalent but are implied by the
 * copybook field widths ({@code CSUSR01Y}) and the {@code SEC-USR-TYPE} 'A'/'U' domain; those
 * messages are marked below and documented in {@code docs/mapping/CS-9-user-admin.md}.</p>
 */
public final class UserAdminMessages {

    /** {@code WHEN FNAMEI = SPACES OR LOW-VALUES}. */
    public static final String FIRST_NAME_EMPTY = "First Name can NOT be empty...";
    /** {@code WHEN LNAMEI = SPACES OR LOW-VALUES}. */
    public static final String LAST_NAME_EMPTY = "Last Name can NOT be empty...";
    /** {@code WHEN USERIDI/USRIDINI = SPACES OR LOW-VALUES}. */
    public static final String USER_ID_EMPTY = "User ID can NOT be empty...";
    /** {@code WHEN PASSWDI = SPACES OR LOW-VALUES}. */
    public static final String PASSWORD_EMPTY = "Password can NOT be empty...";
    /** {@code WHEN USRTYPEI = SPACES OR LOW-VALUES}. */
    public static final String USER_TYPE_EMPTY = "User Type can NOT be empty...";

    /** {@code WRITE-USER-SEC-FILE} {@code DUPKEY/DUPREC} branch (add). */
    public static final String USER_ID_EXISTS = "User ID already exist...";
    /** {@code READ-USER-SEC-FILE}/{@code REWRITE}/{@code DELETE} {@code NOTFND} branch. */
    public static final String USER_ID_NOT_FOUND = "User ID NOT found...";
    /** {@code UPDATE-USER-INFO} — none of the fields differ from the stored record. */
    public static final String NO_CHANGES = "Please modify to update ...";

    /** Extension: {@code SEC-USR-ID PIC X(08)} width. */
    public static final String USER_ID_TOO_LONG = "User ID must be 8 characters or less...";
    /** Extension: {@code SEC-USR-FNAME PIC X(20)} width. */
    public static final String FIRST_NAME_TOO_LONG = "First Name must be 20 characters or less...";
    /** Extension: {@code SEC-USR-LNAME PIC X(20)} width. */
    public static final String LAST_NAME_TOO_LONG = "Last Name must be 20 characters or less...";
    /** Extension: {@code SEC-USR-PWD PIC X(08)} width. */
    public static final String PASSWORD_TOO_LONG = "Password must be 8 characters or less...";
    /** Extension: {@code SEC-USR-TYPE} domain (the copybook 'A'/'U' values). */
    public static final String INVALID_USER_TYPE = "User Type must be 'A' (admin) or 'U' (user)...";

    private UserAdminMessages() {
    }

    public static String added(String userId) {
        return "User " + trim(userId) + " has been added ...";
    }

    public static String updated(String userId) {
        return "User " + trim(userId) + " has been updated ...";
    }

    public static String deleted(String userId) {
        return "User " + trim(userId) + " has been deleted ...";
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
