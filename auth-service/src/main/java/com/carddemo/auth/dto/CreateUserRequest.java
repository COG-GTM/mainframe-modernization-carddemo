package com.carddemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
        @Size(min = 1, max = 8, message = "Password must be between 1 and 8 characters")
        String password,

        @NotBlank(message = "User type is required")
        @Pattern(regexp = "[AaUu]", message = "User type must be 'A' (admin) or 'U' (user)")
        String userType
) {
}
