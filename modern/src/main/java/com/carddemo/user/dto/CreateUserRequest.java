package com.carddemo.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user.
 * <p>
 * Migrated from: COUSR01C.cbl (User Add - CU01)
 * Validation rules match the COBOL program's input validation:
 *   - User ID: max 8 chars, not blank, unique (checked in service)
 *   - First/Last name: max 20 chars, required
 *   - Password: max 8 chars, required
 *   - User type: must be 'A' (admin) or 'U' (user)
 */
public record CreateUserRequest(
        @NotBlank(message = "User ID is required")
        @Size(max = 8, message = "User ID must not exceed 8 characters")
        String userId,

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
