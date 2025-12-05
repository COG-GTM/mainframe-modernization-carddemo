package com.carddemo.user.dto;

import com.carddemo.user.model.User.UserType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update User Request DTO
 * 
 * Maps to the input fields from COUSR02 BMS mapset (Update User screen):
 * - FNAMEI OF COUSR2AI   -> firstName
 * - LNAMEI OF COUSR2AI   -> lastName
 * - PASSWDI OF COUSR2AI  -> password
 * - USRTYPEI OF COUSR2AI -> userType
 * 
 * All fields are optional - only provided fields will be updated.
 * This matches the mainframe behavior where users can modify individual fields.
 * 
 * Original COBOL validation in COUSR02C UPDATE-USER-INFO paragraph:
 * - Checks if each field has changed before updating
 * - Sets USR-MODIFIED-YES flag if any field is modified
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    /**
     * First Name (optional)
     * Max 20 characters as per SEC-USR-FNAME (PIC X(20))
     */
    @Size(max = 20, message = "First Name must be at most 20 characters")
    private String firstName;

    /**
     * Last Name (optional)
     * Max 20 characters as per SEC-USR-LNAME (PIC X(20))
     */
    @Size(max = 20, message = "Last Name must be at most 20 characters")
    private String lastName;

    /**
     * Password (optional)
     * Max 8 characters as per SEC-USR-PWD (PIC X(08))
     */
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    /**
     * User Type (optional)
     */
    private UserType userType;
}
