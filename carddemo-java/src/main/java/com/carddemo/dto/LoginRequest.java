package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank private String userId;
    @NotBlank private String password;
    public LoginRequest() {}
    public LoginRequest(String userId, String password) { this.userId = userId; this.password = password; }
    public String getUserId() { return userId; }
    public void setUserId(String v) { this.userId = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
}
