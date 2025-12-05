package com.carddemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO for authentication.
 * 
 * Maps to COSGN00C.cbl input fields:
 *   USERIDI OF COSGN0AI - User ID input (8 characters max)
 *   PASSWDI OF COSGN0AI - Password input (8 characters max)
 * 
 * Validation rules from COBOL:
 *   WHEN USERIDI OF COSGN0AI = SPACES OR LOW-VALUES
 *       MOVE 'Please enter User ID ...' TO WS-MESSAGE
 *   WHEN PASSWDI OF COSGN0AI = SPACES OR LOW-VALUES
 *       MOVE 'Please enter Password ...' TO WS-MESSAGE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Please enter User ID")
    @Size(max = 8, message = "User ID cannot exceed 8 characters")
    private String userId;

    @NotBlank(message = "Please enter Password")
    @Size(max = 8, message = "Password cannot exceed 8 characters")
    private String password;
}
