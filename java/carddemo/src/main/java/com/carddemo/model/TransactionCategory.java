package com.carddemo.model;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Transaction category type record (TRANCATG). Layout: copybook {@code CVTRA04Y}, LRECL 60. */
@CobolRecord(copybook = "CVTRA04Y", length = 60)
public class TransactionCategory {

    /** {@code TRAN-TYPE-CD PIC X(02)} */
    @CobolField(name = "TRAN-TYPE-CD", offset = 0, length = 2, type = PicType.ALPHANUMERIC)
    private String typeCd;

    /** {@code TRAN-CAT-CD PIC 9(04)} */
    @CobolField(name = "TRAN-CAT-CD", offset = 2, length = 4, type = PicType.UNSIGNED)
    private Integer catCd;

    /** {@code TRAN-CAT-TYPE-DESC PIC X(50)} */
    @CobolField(name = "TRAN-CAT-TYPE-DESC", offset = 6, length = 50, type = PicType.ALPHANUMERIC)
    private String catTypeDesc;

    /** {@code FILLER PIC X(04)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 56, length = 4, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public void setCatCd(Integer catCd) {
        this.catCd = catCd;
    }

    public String getCatTypeDesc() {
        return catTypeDesc;
    }

    public void setCatTypeDesc(String catTypeDesc) {
        this.catTypeDesc = catTypeDesc;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
