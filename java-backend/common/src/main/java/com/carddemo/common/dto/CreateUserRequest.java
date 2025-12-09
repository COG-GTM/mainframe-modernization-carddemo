package com.carddemo.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "User ID is required")
    @Size(max = 8, message = "User ID must not exceed 8 characters")
    private String userId;

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name must not exceed 20 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name must not exceed 20 characters")
    private String lastName;

    @NotBlank(message = "Password is required")
    @Size(max = 8, message = "Password must not exceed 8 characters")
    private String password;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "^[AU]$", message = "User type must be 'A' (Admin) or 'U' (User)")
    private String userType;
}
