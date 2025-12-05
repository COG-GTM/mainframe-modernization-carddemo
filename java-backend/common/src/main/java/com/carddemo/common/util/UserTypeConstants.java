package com.carddemo.common.util;

/**
 * Constants for user types in the CardDemo application.
 * Maps to SEC-USR-TYPE field from CSUSR01Y.cpy copybook.
 * 
 * Original COBOL user types:
 *   - 'A' for Admin users (ADMIN001)
 *   - 'U' for Regular users (USER0001)
 * 
 * The COBOL program COSGN00C checks user type to route to appropriate menu:
 *   IF CDEMO-USRTYP-ADMIN
 *       EXEC CICS XCTL PROGRAM ('COADM01C') ...
 *   ELSE
 *       EXEC CICS XCTL PROGRAM ('COMEN01C') ...
 */
public final class UserTypeConstants {

    private UserTypeConstants() {
        // Prevent instantiation
    }

    public static final String ADMIN = "A";
    public static final String USER = "U";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    public static boolean isAdmin(String userType) {
        return ADMIN.equalsIgnoreCase(userType);
    }

    public static boolean isUser(String userType) {
        return USER.equalsIgnoreCase(userType);
    }

    public static String toRole(String userType) {
        return isAdmin(userType) ? ROLE_ADMIN : ROLE_USER;
    }
}
