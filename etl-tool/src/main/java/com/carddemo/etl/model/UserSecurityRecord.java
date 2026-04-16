package com.carddemo.etl.model;

/**
 * Maps to CSUSR01Y.cpy - User security record (RECLN 80).
 */
public class UserSecurityRecord {

    private String secUsrId;      // PIC X(08)
    private String secUsrFname;   // PIC X(20)
    private String secUsrLname;   // PIC X(20)
    private String secUsrPwd;     // PIC X(08)
    private String secUsrType;    // PIC X(01)

    public String getSecUsrId() { return secUsrId; }
    public void setSecUsrId(String secUsrId) { this.secUsrId = secUsrId; }

    public String getSecUsrFname() { return secUsrFname; }
    public void setSecUsrFname(String secUsrFname) { this.secUsrFname = secUsrFname; }

    public String getSecUsrLname() { return secUsrLname; }
    public void setSecUsrLname(String secUsrLname) { this.secUsrLname = secUsrLname; }

    public String getSecUsrPwd() { return secUsrPwd; }
    public void setSecUsrPwd(String secUsrPwd) { this.secUsrPwd = secUsrPwd; }

    public String getSecUsrType() { return secUsrType; }
    public void setSecUsrType(String secUsrType) { this.secUsrType = secUsrType; }
}
