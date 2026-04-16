package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Java mapping of COBOL copybook CSUSR01Y — User Security Record (RECLN 80).
 * <pre>
 * 01 SEC-USER-DATA.
 *   05 SEC-USR-ID                 PIC X(08)
 *   05 SEC-USR-FNAME              PIC X(20)
 *   05 SEC-USR-LNAME              PIC X(20)
 *   05 SEC-USR-PWD                PIC X(08)
 *   05 SEC-USR-TYPE               PIC X(01)
 *   05 SEC-USR-FILLER             PIC X(23)
 * </pre>
 */
public class SecUserData {

    @JsonProperty("secUsrId")
    private String secUsrId;

    @JsonProperty("secUsrFname")
    private String secUsrFname;

    @JsonProperty("secUsrLname")
    private String secUsrLname;

    @JsonProperty("secUsrPwd")
    private String secUsrPwd;

    @JsonProperty("secUsrType")
    private String secUsrType;

    public SecUserData() {
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

    /**
     * Returns the typed UserType enum for this user's type code.
     */
    @JsonIgnore
    public UserType getUserType() {
        if (secUsrType == null || secUsrType.isEmpty()) {
            return null;
        }
        return UserType.fromCode(secUsrType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecUserData that = (SecUserData) o;
        return Objects.equals(secUsrId, that.secUsrId)
                && Objects.equals(secUsrFname, that.secUsrFname)
                && Objects.equals(secUsrLname, that.secUsrLname)
                && Objects.equals(secUsrPwd, that.secUsrPwd)
                && Objects.equals(secUsrType, that.secUsrType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secUsrId, secUsrFname, secUsrLname, secUsrPwd, secUsrType);
    }

    @Override
    public String toString() {
        return "SecUserData{" +
                "secUsrId='" + secUsrId + '\'' +
                ", secUsrFname='" + secUsrFname + '\'' +
                ", secUsrLname='" + secUsrLname + '\'' +
                ", secUsrType='" + secUsrType + '\'' +
                '}';
    }
}
