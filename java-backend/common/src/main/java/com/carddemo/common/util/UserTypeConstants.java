package com.carddemo.common.util;

public final class UserTypeConstants {
    public static final String ADMIN = "A";
    public static final String USER = "U";

    private UserTypeConstants() {
    }

    public static boolean isAdmin(String userType) {
        return ADMIN.equals(userType);
    }

    public static boolean isUser(String userType) {
        return USER.equals(userType);
    }

    public static boolean isValidUserType(String userType) {
        return ADMIN.equals(userType) || USER.equals(userType);
    }

    public static String getDisplayName(String userType) {
        if (ADMIN.equals(userType)) {
            return "Administrator";
        } else if (USER.equals(userType)) {
            return "Regular User";
        }
        return "Unknown";
    }
}
