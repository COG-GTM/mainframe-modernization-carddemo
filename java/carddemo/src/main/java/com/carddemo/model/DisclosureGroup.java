package com.carddemo.model;

import java.math.BigDecimal;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Disclosure group record carrying the interest rate (DISCGRP). Layout: copybook {@code CVTRA02Y}, LRECL 50. */
@CobolRecord(copybook = "CVTRA02Y", length = 50)
public class DisclosureGroup {

    /** {@code DIS-ACCT-GROUP-ID PIC X(10)} */
    @CobolField(name = "DIS-ACCT-GROUP-ID", offset = 0, length = 10, type = PicType.ALPHANUMERIC)
    private String acctGroupId;

    /** {@code DIS-TRAN-TYPE-CD PIC X(02)} */
    @CobolField(name = "DIS-TRAN-TYPE-CD", offset = 10, length = 2, type = PicType.ALPHANUMERIC)
    private String tranTypeCd;

    /** {@code DIS-TRAN-CAT-CD PIC 9(04)} */
    @CobolField(name = "DIS-TRAN-CAT-CD", offset = 12, length = 4, type = PicType.UNSIGNED)
    private Integer tranCatCd;

    /** {@code DIS-INT-RATE PIC S9(04)V99} */
    @CobolField(name = "DIS-INT-RATE", offset = 16, length = 6, type = PicType.SIGNED, scale = 2)
    private BigDecimal intRate;

    /** {@code FILLER PIC X(28)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 22, length = 28, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public BigDecimal getIntRate() {
        return intRate;
    }

    public void setIntRate(BigDecimal intRate) {
        this.intRate = intRate;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
