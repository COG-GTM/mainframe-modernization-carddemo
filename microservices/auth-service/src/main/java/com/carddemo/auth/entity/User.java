package com.carddemo.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User entity mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA).
 *
 * Original COBOL structure:
 *   05 SEC-USR-ID      PIC X(08)
 *   05 SEC-USR-FNAME   PIC X(20)
 *   05 SEC-USR-LNAME   PIC X(20)
 *   05 SEC-USR-PWD     PIC X(08)
 *   05 SEC-USR-TYPE    PIC X(01)  -- 'A' = admin, 'U' = regular
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "usr_id", length = 8, nullable = false)
    private String usrId;

    @Column(name = "usr_fname", length = 20)
    private String usrFname;

    @Column(name = "usr_lname", length = 20)
    private String usrLname;

    @Column(name = "usr_pwd", length = 100, nullable = false)
    private String usrPwd;

    @Column(name = "usr_type", length = 1, nullable = false, columnDefinition = "CHAR(1)")
    private String usrType;

    public User() {
    }

    public User(String usrId, String usrFname, String usrLname, String usrPwd, String usrType) {
        this.usrId = usrId;
        this.usrFname = usrFname;
        this.usrLname = usrLname;
        this.usrPwd = usrPwd;
        this.usrType = usrType;
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getUsrFname() {
        return usrFname;
    }

    public void setUsrFname(String usrFname) {
        this.usrFname = usrFname;
    }

    public String getUsrLname() {
        return usrLname;
    }

    public void setUsrLname(String usrLname) {
        this.usrLname = usrLname;
    }

    public String getUsrPwd() {
        return usrPwd;
    }

    public void setUsrPwd(String usrPwd) {
        this.usrPwd = usrPwd;
    }

    public String getUsrType() {
        return usrType;
    }

    public void setUsrType(String usrType) {
        this.usrType = usrType;
    }
}
