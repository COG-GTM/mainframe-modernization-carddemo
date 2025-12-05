package com.carddemo.user.dto;

import com.carddemo.user.model.User.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User DTO for API responses
 * 
 * Maps to the display fields shown in COUSR00 (user list) screen:
 * - USRID01I-USRID10I -> userId
 * - FNAME01I-FNAME10I -> firstName
 * - LNAME01I-LNAME10I -> lastName
 * - UTYPE01I-UTYPE10I -> userType
 * 
 * Note: Password is never returned in responses for security
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * User ID
     * Maps to SEC-USR-ID
     */
    private String userId;

    /**
     * First Name
     * Maps to SEC-USR-FNAME
     */
    private String firstName;

    /**
     * Last Name
     * Maps to SEC-USR-LNAME
     */
    private String lastName;

    /**
     * User Type (ADMIN or USER)
     * Maps to SEC-USR-TYPE
     */
    private UserType userType;
}
