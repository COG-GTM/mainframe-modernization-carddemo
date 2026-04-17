package com.carddemo.useradmin.dto;

/**
 * Response DTO for user data.
 * Never includes the password field — mirrors the COBOL user list screen
 * (COUSR00C) which displays userId, firstName, lastName, and userType only.
 */
public class UserResponse {

    private String userId;
    private String firstName;
    private String lastName;
    private String userType;

    public UserResponse() {
    }

    public UserResponse(String userId, String firstName, String lastName, String userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
