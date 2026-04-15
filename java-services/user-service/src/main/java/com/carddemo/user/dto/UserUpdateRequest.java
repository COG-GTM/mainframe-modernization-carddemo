package com.carddemo.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing user.
 * Mirrors validation from COUSR02C (User Update):
 *   - Fields are optional; only provided fields are updated
 *   - If userType is provided, it must be 'A' or 'U'
 */
public class UserUpdateRequest {

    @Size(max = 20, message = "First Name must be at most 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last Name must be at most 20 characters")
    private String lastName;

    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    @Pattern(regexp = "[AaUu]", message = "User Type must be 'A' (Admin) or 'U' (User)")
    private String userType;

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(String firstName, String lastName, String password, String userType) {
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
