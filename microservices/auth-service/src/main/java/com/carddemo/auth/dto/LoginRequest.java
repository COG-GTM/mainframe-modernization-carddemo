package com.carddemo.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request DTO.
 * Maps to the COSGN0AI screen input fields: USERIDI and PASSWDI.
 */
public class LoginRequest {

    @NotBlank(message = "Please enter User ID ...")
    private String userId;

    @NotBlank(message = "Please enter Password ...")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
