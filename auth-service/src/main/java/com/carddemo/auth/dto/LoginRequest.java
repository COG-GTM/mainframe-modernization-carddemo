package com.carddemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Please enter User ID ...")
        @Size(max = 8, message = "User ID must not exceed 8 characters")
        String userId,

        @NotBlank(message = "Please enter Password ...")
        @Size(max = 8, message = "Password must not exceed 8 characters")
        String password
) {
}
