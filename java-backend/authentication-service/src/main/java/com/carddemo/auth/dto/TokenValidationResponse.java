package com.carddemo.auth.dto;

import com.carddemo.auth.model.User.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token validation response DTO
 * 
 * Used by other services (via API Gateway) to validate JWT tokens
 * and retrieve user information for authorization decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    /**
     * Whether the token is valid
     */
    private boolean valid;

    /**
     * User ID extracted from the token
     */
    private String userId;

    /**
     * User type for authorization decisions
     */
    private UserType userType;

    /**
     * Error message if token is invalid
     */
    private String errorMessage;
}
