package com.carddemo.model;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Transaction type record (TRANTYPE). Layout: copybook {@code CVTRA03Y}, LRECL 60. */
@CobolRecord(copybook = "CVTRA03Y", length = 60)
public class TransactionType {

    /** {@code TRAN-TYPE PIC X(02)} */
    @CobolField(name = "TRAN-TYPE", offset = 0, length = 2, type = PicType.ALPHANUMERIC)
    private String type;

    /** {@code TRAN-TYPE-DESC PIC X(50)} */
    @CobolField(name = "TRAN-TYPE-DESC", offset = 2, length = 50, type = PicType.ALPHANUMERIC)
    private String typeDesc;

    /** {@code FILLER PIC X(08)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 52, length = 8, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
