package com.carddemo.dto;

public class UserUpdateRequest {
    private String usrFname;
    private String usrLname;
    private String usrPwd;
    private String usrType;
    public UserUpdateRequest() {}
    public String getUsrFname() { return usrFname; }
    public void setUsrFname(String v) { this.usrFname = v; }
    public String getUsrLname() { return usrLname; }
    public void setUsrLname(String v) { this.usrLname = v; }
    public String getUsrPwd() { return usrPwd; }
    public void setUsrPwd(String v) { this.usrPwd = v; }
    public String getUsrType() { return usrType; }
    public void setUsrType(String v) { this.usrType = v; }
}
