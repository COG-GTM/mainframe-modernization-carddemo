package com.carddemo.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user.
 * Mirrors validation from COUSR01C (User Add):
 *   - User ID, first name, last name, password, user type are all required
 *   - User type must be 'A' (Admin) or 'U' (regular User)
 */
public class UserCreateRequest {

    @NotBlank(message = "User ID can NOT be empty")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    private String userId;

    @NotBlank(message = "First Name can NOT be empty")
    @Size(max = 20, message = "First Name must be at most 20 characters")
    private String firstName;

    @NotBlank(message = "Last Name can NOT be empty")
    @Size(max = 20, message = "Last Name must be at most 20 characters")
    private String lastName;

    @NotBlank(message = "Password can NOT be empty")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    @NotBlank(message = "User Type can NOT be empty")
    @Pattern(regexp = "[AaUu]", message = "User Type must be 'A' (Admin) or 'U' (User)")
    private String userType;

    public UserCreateRequest() {
    }

    public UserCreateRequest(String userId, String firstName, String lastName, String password, String userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
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
