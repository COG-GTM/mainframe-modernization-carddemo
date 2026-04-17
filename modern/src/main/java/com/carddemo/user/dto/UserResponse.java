package com.carddemo.user.dto;

/**
 * Response DTO for user data (excludes password for security).
 * <p>
 * Migrated from: CSUSR01Y.cpy (SEC-USER-DATA layout)
 * The password field (SEC-USR-PWD) is intentionally excluded from responses.
 */
public record UserResponse(
        String userId,
        String firstName,
        String lastName,
        String userType
) {
}
