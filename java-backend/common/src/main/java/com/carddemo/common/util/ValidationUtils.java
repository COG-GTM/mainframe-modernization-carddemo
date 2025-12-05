package com.carddemo.common.util;

import com.carddemo.common.exception.ValidationException;

/**
 * Utility class for input validation.
 * Implements validation rules from COBOL programs.
 * 
 * Original COBOL validation examples from COUSR01C.cbl:
 *   WHEN FNAMEI OF COUSR1AI = SPACES OR LOW-VALUES
 *       MOVE 'First Name can NOT be empty...' TO WS-MESSAGE
 *   WHEN USERIDI OF COUSR1AI = SPACES OR LOW-VALUES
 *       MOVE 'User ID can NOT be empty...' TO WS-MESSAGE
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Prevent instantiation
    }

    public static final int USER_ID_MAX_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 8;
    public static final int FIRST_NAME_MAX_LENGTH = 20;
    public static final int LAST_NAME_MAX_LENGTH = 20;

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "cannot be empty");
        }
    }

    public static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(fieldName, 
                    String.format("cannot exceed %d characters", maxLength));
        }
    }

    public static void validateUserId(String userId) {
        validateNotEmpty(userId, "userId");
        validateMaxLength(userId, USER_ID_MAX_LENGTH, "userId");
    }

    public static void validatePassword(String password) {
        validateNotEmpty(password, "password");
        validateMaxLength(password, PASSWORD_MAX_LENGTH, "password");
    }

    public static void validateUserType(String userType) {
        validateNotEmpty(userType, "userType");
        if (!UserTypeConstants.isAdmin(userType) && !UserTypeConstants.isUser(userType)) {
            throw new ValidationException("userType", "must be 'A' (Admin) or 'U' (User)");
        }
    }
}
