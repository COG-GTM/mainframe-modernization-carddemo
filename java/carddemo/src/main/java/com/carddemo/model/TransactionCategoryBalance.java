package com.carddemo.model;

import java.math.BigDecimal;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Transaction category balance record (TCATBALF). Layout: copybook {@code CVTRA01Y}, LRECL 50. */
@CobolRecord(copybook = "CVTRA01Y", length = 50)
public class TransactionCategoryBalance {

    /** {@code TRANCAT-ACCT-ID PIC 9(11)} */
    @CobolField(name = "TRANCAT-ACCT-ID", offset = 0, length = 11, type = PicType.UNSIGNED)
    private Long trancatAcctId;

    /** {@code TRANCAT-TYPE-CD PIC X(02)} */
    @CobolField(name = "TRANCAT-TYPE-CD", offset = 11, length = 2, type = PicType.ALPHANUMERIC)
    private String trancatTypeCd;

    /** {@code TRANCAT-CD PIC 9(04)} */
    @CobolField(name = "TRANCAT-CD", offset = 13, length = 4, type = PicType.UNSIGNED)
    private Integer trancatCd;

    /** {@code TRAN-CAT-BAL PIC S9(09)V99} */
    @CobolField(name = "TRAN-CAT-BAL", offset = 17, length = 11, type = PicType.SIGNED, scale = 2)
    private BigDecimal tranCatBal;

    /** {@code FILLER PIC X(22)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 28, length = 22, type = PicType.ALPHANUMERIC)
    private String filler;

    public Long getTrancatAcctId() {
        return trancatAcctId;
    }

    public void setTrancatAcctId(Long trancatAcctId) {
        this.trancatAcctId = trancatAcctId;
    }

    public String getTrancatTypeCd() {
        return trancatTypeCd;
    }

    public void setTrancatTypeCd(String trancatTypeCd) {
        this.trancatTypeCd = trancatTypeCd;
    }

    public Integer getTrancatCd() {
        return trancatCd;
    }

    public void setTrancatCd(Integer trancatCd) {
        this.trancatCd = trancatCd;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
