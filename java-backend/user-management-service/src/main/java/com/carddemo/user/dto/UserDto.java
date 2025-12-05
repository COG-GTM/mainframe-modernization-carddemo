package com.carddemo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User DTO for API responses.
 * 
 * Maps to user display in COUSR00C.cbl:
 *   USRID01I  - User ID
 *   FNAME01I  - First Name
 *   LNAME01I  - Last Name
 *   UTYPE01I  - User Type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String userId;
    private String firstName;
    private String lastName;
    private String userType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
