package com.carddemo.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new user.
 * 
 * Maps to COUSR01C.cbl input fields:
 *   USERIDI  OF COUSR1AI - User ID (8 characters max)
 *   FNAMEI   OF COUSR1AI - First Name (20 characters max)
 *   LNAMEI   OF COUSR1AI - Last Name (20 characters max)
 *   PASSWDI  OF COUSR1AI - Password (8 characters max)
 *   USRTYPEI OF COUSR1AI - User Type (A or U)
 * 
 * Validation rules from COBOL:
 *   WHEN FNAMEI OF COUSR1AI = SPACES OR LOW-VALUES
 *       MOVE 'First Name can NOT be empty...' TO WS-MESSAGE
 *   WHEN LNAMEI OF COUSR1AI = SPACES OR LOW-VALUES
 *       MOVE 'Last Name can NOT be empty...' TO WS-MESSAGE
 *   WHEN USERIDI OF COUSR1AI = SPACES OR LOW-VALUES
 *       MOVE 'User ID can NOT be empty...' TO WS-MESSAGE
 *   WHEN PASSWDI OF COUSR1AI = SPACES OR LOW-VALUES
 *       MOVE 'Password can NOT be empty...' TO WS-MESSAGE
 *   WHEN USRTYPEI OF COUSR1AI = SPACES OR LOW-VALUES
 *       MOVE 'User Type can NOT be empty...' TO WS-MESSAGE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "User ID can NOT be empty")
    @Size(max = 8, message = "User ID cannot exceed 8 characters")
    private String userId;

    @NotBlank(message = "First Name can NOT be empty")
    @Size(max = 20, message = "First Name cannot exceed 20 characters")
    private String firstName;

    @NotBlank(message = "Last Name can NOT be empty")
    @Size(max = 20, message = "Last Name cannot exceed 20 characters")
    private String lastName;

    @NotBlank(message = "Password can NOT be empty")
    @Size(max = 8, message = "Password cannot exceed 8 characters")
    private String password;

    @NotBlank(message = "User Type can NOT be empty")
    @Pattern(regexp = "^[AUau]$", message = "User Type must be 'A' (Admin) or 'U' (User)")
    private String userType;
}
