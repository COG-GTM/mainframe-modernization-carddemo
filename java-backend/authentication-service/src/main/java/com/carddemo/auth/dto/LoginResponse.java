package com.carddemo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO containing authentication result.
 * 
 * Replaces COBOL COMMAREA data passed between programs:
 *   CDEMO-USER-ID   - Authenticated user ID
 *   CDEMO-USER-TYPE - User type (A=Admin, U=User)
 * 
 * The JWT token replaces the CICS session management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String userId;
    private String firstName;
    private String lastName;
    private String userType;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
