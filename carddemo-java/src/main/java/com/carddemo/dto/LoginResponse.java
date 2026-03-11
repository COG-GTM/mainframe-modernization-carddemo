package com.carddemo.dto;

public class LoginResponse {
    private String token;
    private String userType;
    private String firstName;
    private String lastName;
    public LoginResponse() {}
    public LoginResponse(String token, String userType, String firstName, String lastName) {
        this.token = token; this.userType = userType; this.firstName = firstName; this.lastName = lastName;
    }
    public String getToken() { return token; }
    public void setToken(String v) { this.token = v; }
    public String getUserType() { return userType; }
    public void setUserType(String v) { this.userType = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
}
