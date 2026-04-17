package com.carddemo.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing user.
 * <p>
 * Migrated from: COUSR02C.cbl (User Update - CU02)
 * Validation rules match the COBOL program's update validation.
 * The userId is provided via the path parameter, not in the request body.
 */
public record UpdateUserRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 20, message = "First name must not exceed 20 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 20, message = "Last name must not exceed 20 characters")
        String lastName,

        @NotBlank(message = "Password is required")
        @Size(max = 8, message = "Password must not exceed 8 characters")
        String password,

        @NotBlank(message = "User type is required")
        @Pattern(regexp = "[AUau]", message = "User type must be 'A' (admin) or 'U' (user)")
        String userType
) {
}
