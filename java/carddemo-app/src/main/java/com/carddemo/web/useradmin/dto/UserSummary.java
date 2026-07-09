package com.carddemo.web.useradmin.dto;

import com.carddemo.domain.SecurityUser;

/**
 * One user row as shown on the {@code COUSR00} list screen (and echoed by add/update).
 *
 * @param userId    SEC-USR-ID PIC X(08)
 * @param firstName SEC-USR-FNAME PIC X(20)
 * @param lastName  SEC-USR-LNAME PIC X(20)
 * @param userType  SEC-USR-TYPE PIC X(01) ('A' / 'U')
 */
public record UserSummary(String userId, String firstName, String lastName, String userType) {

    public static UserSummary from(SecurityUser user) {
        return new UserSummary(
                trim(user.getSecUsrId()),
                trim(user.getSecUsrFname()),
                trim(user.getSecUsrLname()),
                trim(user.getSecUsrType()));
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
