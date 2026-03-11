package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;

public class UserSecurityRequest {
    @NotBlank private String usrId;
    @NotBlank private String usrFname;
    @NotBlank private String usrLname;
    @NotBlank private String usrPwd;
    @NotBlank private String usrType;
    public UserSecurityRequest() {}
    public String getUsrId() { return usrId; }
    public void setUsrId(String v) { this.usrId = v; }
    public String getUsrFname() { return usrFname; }
    public void setUsrFname(String v) { this.usrFname = v; }
    public String getUsrLname() { return usrLname; }
    public void setUsrLname(String v) { this.usrLname = v; }
    public String getUsrPwd() { return usrPwd; }
    public void setUsrPwd(String v) { this.usrPwd = v; }
    public String getUsrType() { return usrType; }
    public void setUsrType(String v) { this.usrType = v; }
}
