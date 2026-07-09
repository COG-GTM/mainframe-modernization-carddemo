package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Security user — maps COBOL copybook {@code CSUSR01Y} (SEC-USER-DATA, RECLN 80).
 *
 * <p>Backs the USRSEC VSAM KSDS (KEYS(8,0) on SEC-USR-ID per {@code DUSRSECJ.jcl}). Seed
 * users are the inline records from {@code DUSRSECJ.jcl}. CS-2 security builds on this.</p>
 */
@Entity
@Table(name = "sec_user")
public class SecurityUser {

    /** SEC-USR-ID PIC X(08) — 8-char user id (VSAM key). */
    @Id
    @Column(name = "sec_usr_id", length = 8, nullable = false)
    private String secUsrId;

    /** SEC-USR-FNAME PIC X(20). */
    @Column(name = "sec_usr_fname", length = 20)
    private String secUsrFname;

    /** SEC-USR-LNAME PIC X(20). */
    @Column(name = "sec_usr_lname", length = 20)
    private String secUsrLname;

    /** SEC-USR-PWD PIC X(08). */
    @Column(name = "sec_usr_pwd", length = 8)
    private String secUsrPwd;

    /** SEC-USR-TYPE PIC X(01) — 'A' admin, 'U' user. */
    @Column(name = "sec_usr_type", length = 1)
    private String secUsrType;

    public SecurityUser() {
    }

    public String getSecUsrId() {
        return secUsrId;
    }

    public void setSecUsrId(String secUsrId) {
        this.secUsrId = secUsrId;
    }

    public String getSecUsrFname() {
        return secUsrFname;
    }

    public void setSecUsrFname(String secUsrFname) {
        this.secUsrFname = secUsrFname;
    }

    public String getSecUsrLname() {
        return secUsrLname;
    }

    public void setSecUsrLname(String secUsrLname) {
        this.secUsrLname = secUsrLname;
    }

    public String getSecUsrPwd() {
        return secUsrPwd;
    }

    public void setSecUsrPwd(String secUsrPwd) {
        this.secUsrPwd = secUsrPwd;
    }

    public String getSecUsrType() {
        return secUsrType;
    }

    public void setSecUsrType(String secUsrType) {
        this.secUsrType = secUsrType;
    }
}
