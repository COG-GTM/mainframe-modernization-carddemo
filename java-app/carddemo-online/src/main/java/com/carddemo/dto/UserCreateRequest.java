package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new user.
 * Mirrors COUSR02C.cbl add user logic.
 */
public class UserCreateRequest {

    @NotBlank(message = "User ID is required")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    private String userId;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 8, message = "Password must be between 4 and 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name must be at most 20 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name must be at most 20 characters")
    private String lastName;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "ADMIN|USER", message = "User type must be ADMIN or USER")
    private String userType;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
