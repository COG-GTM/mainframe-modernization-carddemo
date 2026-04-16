package com.carddemo.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 20, message = "First name must not exceed 20 characters")
        String firstName,

        @Size(max = 20, message = "Last name must not exceed 20 characters")
        String lastName,

        @Size(min = 1, max = 8, message = "Password must be between 1 and 8 characters")
        String password,

        @Pattern(regexp = "[AaUu]", message = "User type must be 'A' (admin) or 'U' (user)")
        String userType
) {
}
