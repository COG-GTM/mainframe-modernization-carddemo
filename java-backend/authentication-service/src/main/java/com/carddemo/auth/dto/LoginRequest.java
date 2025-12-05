package com.carddemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO
 * 
 * Maps to the input fields from COSGN00 BMS mapset:
 * - USERIDI OF COSGN0AI -> userId
 * - PASSWDI OF COSGN0AI -> password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * User ID for authentication
     * Corresponds to USERIDI field in COSGN00 screen
     * Max 8 characters as per SEC-USR-ID (PIC X(08))
     */
    @NotBlank(message = "Please enter User ID ...")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    private String userId;

    /**
     * Password for authentication
     * Corresponds to PASSWDI field in COSGN00 screen
     * Max 8 characters as per SEC-USR-PWD (PIC X(08))
     */
    @NotBlank(message = "Please enter Password ...")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;
}
