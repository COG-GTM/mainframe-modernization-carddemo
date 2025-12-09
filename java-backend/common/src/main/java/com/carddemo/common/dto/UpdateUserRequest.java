package com.carddemo.common.dto;

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
public class UpdateUserRequest {
    @Size(max = 20, message = "First name must not exceed 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last name must not exceed 20 characters")
    private String lastName;

    @Size(max = 8, message = "Password must not exceed 8 characters")
    private String password;

    @Pattern(regexp = "^[AU]$", message = "User type must be 'A' (Admin) or 'U' (User)")
    private String userType;
}
