package com.carddemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login request DTO matching the COSGN00C sign-on screen input fields.
 *
 * Migrated from: COSGN00C.cbl, PROCESS-ENTER-KEY paragraph (lines 108-140)
 * Original BMS map fields: USERIDI OF COSGN0AI, PASSWDI OF COSGN0AI
 */
public record LoginRequest(
        @NotBlank(message = "Please enter User ID ...")
        @Size(max = 8, message = "User ID must not exceed 8 characters")
        String userId,

        @NotBlank(message = "Please enter Password ...")
        @Size(max = 8, message = "Password must not exceed 8 characters")
        String password
) {
}
