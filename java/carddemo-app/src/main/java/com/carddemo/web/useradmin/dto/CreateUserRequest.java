package com.carddemo.web.useradmin.dto;

/**
 * Body of {@code POST /api/admin/users} — the {@code COUSR01C} add-user input fields
 * ({@code USERIDI}, {@code FNAMEI}, {@code LNAMEI}, {@code PASSWDI}, {@code USRTYPEI}).
 *
 * @param userId    SEC-USR-ID PIC X(08)
 * @param firstName SEC-USR-FNAME PIC X(20)
 * @param lastName  SEC-USR-LNAME PIC X(20)
 * @param password  SEC-USR-PWD PIC X(08) — legacy plaintext (stored {@code {noop}}-compatible)
 * @param userType  SEC-USR-TYPE PIC X(01) ('A' / 'U')
 */
public record CreateUserRequest(
        String userId,
        String firstName,
        String lastName,
        String password,
        String userType) {
}
