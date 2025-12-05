package com.carddemo.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing user.
 * 
 * Maps to COUSR02C.cbl input fields:
 *   FNAMEI   OF COUSR2AI - First Name
 *   LNAMEI   OF COUSR2AI - Last Name
 *   PASSWDI  OF COUSR2AI - Password
 *   USRTYPEI OF COUSR2AI - User Type
 * 
 * Note: User ID cannot be changed (it's the primary key)
 * 
 * COBOL update logic from COUSR02C.cbl:
 *   IF FNAMEI  OF COUSR2AI NOT = SEC-USR-FNAME
 *       MOVE FNAMEI   OF COUSR2AI TO SEC-USR-FNAME
 *       SET USR-MODIFIED-YES TO TRUE
 *   END-IF
 *   [similar for other fields]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 20, message = "First Name cannot exceed 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last Name cannot exceed 20 characters")
    private String lastName;

    @Size(max = 8, message = "Password cannot exceed 8 characters")
    private String password;

    @Pattern(regexp = "^[AUau]$", message = "User Type must be 'A' (Admin) or 'U' (User)")
    private String userType;
}
