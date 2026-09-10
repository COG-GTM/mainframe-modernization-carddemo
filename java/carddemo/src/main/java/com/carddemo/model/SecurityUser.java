package com.carddemo.model;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** User security record (USRSEC). Layout: copybook {@code CSUSR01Y}, LRECL 80. */
@CobolRecord(copybook = "CSUSR01Y", length = 80)
public class SecurityUser {

    /** {@code SEC-USR-ID PIC X(08)} */
    @CobolField(name = "SEC-USR-ID", offset = 0, length = 8, type = PicType.ALPHANUMERIC)
    private String id;

    /** {@code SEC-USR-FNAME PIC X(20)} */
    @CobolField(name = "SEC-USR-FNAME", offset = 8, length = 20, type = PicType.ALPHANUMERIC)
    private String fname;

    /** {@code SEC-USR-LNAME PIC X(20)} */
    @CobolField(name = "SEC-USR-LNAME", offset = 28, length = 20, type = PicType.ALPHANUMERIC)
    private String lname;

    /** {@code SEC-USR-PWD PIC X(08)} */
    @CobolField(name = "SEC-USR-PWD", offset = 48, length = 8, type = PicType.ALPHANUMERIC)
    private String pwd;

    /** {@code SEC-USR-TYPE PIC X(01)} */
    @CobolField(name = "SEC-USR-TYPE", offset = 56, length = 1, type = PicType.ALPHANUMERIC)
    private String type;

    /** {@code FILLER PIC X(23)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 57, length = 23, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
