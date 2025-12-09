package com.carddemo.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "User ID is required")
    @Size(max = 8, message = "User ID must not exceed 8 characters")
    private String userId;

    @NotBlank(message = "Password is required")
    @Size(max = 8, message = "Password must not exceed 8 characters")
    private String password;
}
