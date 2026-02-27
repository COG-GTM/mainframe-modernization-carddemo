package com.carddemo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_security")
public class UserSecurity {
    @Id @Column(name = "usr_id", length = 8) private String usrId;
    @Column(name = "usr_fname", length = 20) private String usrFname;
    @Column(name = "usr_lname", length = 20) private String usrLname;
    @Column(name = "usr_pwd", length = 8) private String usrPwd;
    @Column(name = "usr_type", length = 1) private String usrType;
    public UserSecurity() {}
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
