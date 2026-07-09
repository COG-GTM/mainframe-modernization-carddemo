package com.carddemo.web.useradmin.dto;

import com.carddemo.domain.SecurityUser;

/**
 * Response for a single-user mutation (add / update) — the resulting user plus the verbatim
 * {@code COUSRxxC} confirmation message (e.g. {@code "User NEWUSR01 has been added ..."}).
 *
 * @param userId    SEC-USR-ID PIC X(08)
 * @param firstName SEC-USR-FNAME PIC X(20)
 * @param lastName  SEC-USR-LNAME PIC X(20)
 * @param userType  SEC-USR-TYPE PIC X(01) ('A' / 'U')
 * @param message   the operator confirmation / info message
 */
public record UserResponse(
        String userId,
        String firstName,
        String lastName,
        String userType,
        String message) {

    public static UserResponse from(SecurityUser user, String message) {
        return new UserResponse(
                trim(user.getSecUsrId()),
                trim(user.getSecUsrFname()),
                trim(user.getSecUsrLname()),
                trim(user.getSecUsrType()),
                message);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
