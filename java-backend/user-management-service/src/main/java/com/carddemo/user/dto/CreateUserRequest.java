package com.carddemo.user.dto;

import com.carddemo.user.model.User.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create User Request DTO
 * 
 * Maps to the input fields from COUSR01 BMS mapset (Add User screen):
 * - FNAMEI OF COUSR1AI   -> firstName
 * - LNAMEI OF COUSR1AI   -> lastName
 * - USERIDI OF COUSR1AI  -> userId
 * - PASSWDI OF COUSR1AI  -> password
 * - USRTYPEI OF COUSR1AI -> userType
 * 
 * Validation messages match the original COBOL error messages from COUSR01C.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    /**
     * User ID for the new user
     * Max 8 characters as per SEC-USR-ID (PIC X(08))
     */
    @NotBlank(message = "User ID can NOT be empty...")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    private String userId;

    /**
     * First Name
     * Max 20 characters as per SEC-USR-FNAME (PIC X(20))
     */
    @NotBlank(message = "First Name can NOT be empty...")
    @Size(max = 20, message = "First Name must be at most 20 characters")
    private String firstName;

    /**
     * Last Name
     * Max 20 characters as per SEC-USR-LNAME (PIC X(20))
     */
    @NotBlank(message = "Last Name can NOT be empty...")
    @Size(max = 20, message = "Last Name must be at most 20 characters")
    private String lastName;

    /**
     * Password
     * Max 8 characters as per SEC-USR-PWD (PIC X(08))
     */
    @NotBlank(message = "Password can NOT be empty...")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    /**
     * User Type (ADMIN or USER)
     */
    @NotNull(message = "User Type can NOT be empty...")
    private UserType userType;
}
