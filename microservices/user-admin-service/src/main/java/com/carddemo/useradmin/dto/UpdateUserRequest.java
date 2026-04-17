package com.carddemo.useradmin.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing user.
 * Corresponds to COUSR02C (User Update) screen input fields.
 * All fields are optional — only provided fields are updated.
 * Password is optional: only hashed and updated if provided.
 */
public class UpdateUserRequest {

    @Size(max = 20, message = "First name must be at most 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last name must be at most 20 characters")
    private String lastName;

    private String password;

    @Pattern(regexp = "[AUau]", message = "User type must be 'A' (Admin) or 'U' (User)")
    private String userType;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String firstName, String lastName, String password, String userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
