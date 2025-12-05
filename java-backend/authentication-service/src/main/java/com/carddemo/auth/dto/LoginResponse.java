package com.carddemo.auth.dto;

import com.carddemo.auth.model.User.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO containing JWT token and user information
 * 
 * After successful authentication in the mainframe (COSGN00C), the system:
 * - Sets CDEMO-USER-ID and CDEMO-USER-TYPE in CARDDEMO-COMMAREA
 * - Routes to COADM01C (admin menu) or COMEN01C (user menu) based on type
 * 
 * In the modernized system, we return a JWT token instead of using COMMAREA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT access token for subsequent API calls
     * Replaces the CARDDEMO-COMMAREA session management
     */
    private String accessToken;

    /**
     * Token type (always "Bearer")
     */
    private String tokenType;

    /**
     * Token expiration time in seconds
     */
    private Long expiresIn;

    /**
     * User ID of the authenticated user
     * Maps to CDEMO-USER-ID in CARDDEMO-COMMAREA
     */
    private String userId;

    /**
     * User type (ADMIN or USER)
     * Maps to CDEMO-USER-TYPE in CARDDEMO-COMMAREA
     * Determines which menu/features the user can access
     */
    private UserType userType;

    /**
     * User's first name for display purposes
     */
    private String firstName;

    /**
     * User's last name for display purposes
     */
    private String lastName;
}
