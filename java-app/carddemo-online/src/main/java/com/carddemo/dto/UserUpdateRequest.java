package com.carddemo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing user.
 * Mirrors COUSR03C.cbl update user logic.
 */
public class UserUpdateRequest {

    @Size(min = 4, max = 8, message = "Password must be between 4 and 8 characters")
    private String password;

    @Size(max = 20, message = "First name must be at most 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last name must be at most 20 characters")
    private String lastName;

    @Pattern(regexp = "ADMIN|USER", message = "User type must be ADMIN or USER")
    private String userType;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
