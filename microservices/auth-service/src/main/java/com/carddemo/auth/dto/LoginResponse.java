package com.carddemo.auth.dto;

/**
 * Login response DTO.
 * Contains the JWT token and user routing information.
 * In the original COBOL, successful auth would XCTL to COADM01C (admin) or COMEN01C (regular).
 */
public class LoginResponse {

    private String token;
    private String userId;
    private String userType;
    private String firstName;
    private String lastName;
    private String redirectProgram;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userId, String userType,
                         String firstName, String lastName, String redirectProgram) {
        this.token = token;
        this.userId = userId;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.redirectProgram = redirectProgram;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
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

    public String getRedirectProgram() {
        return redirectProgram;
    }

    public void setRedirectProgram(String redirectProgram) {
        this.redirectProgram = redirectProgram;
    }
}
