package com.carddemo.web.dto;

/** Successful signon, including the menu the COBOL program would have transferred control to. */
public record SignonResponse(
        String userId,
        String firstName,
        String lastName,
        String userType,
        boolean admin,
        String legacyMenuProgram) {
}
