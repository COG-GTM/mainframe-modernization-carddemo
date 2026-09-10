package com.carddemo.model;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Card / account / customer cross reference record (CARDXREF). Layout: copybook {@code CVACT03Y}, LRECL 50. */
@CobolRecord(copybook = "CVACT03Y", length = 50)
public class CardXref {

    /** {@code XREF-CARD-NUM PIC X(16)} */
    @CobolField(name = "XREF-CARD-NUM", offset = 0, length = 16, type = PicType.ALPHANUMERIC)
    private String cardNum;

    /** {@code XREF-CUST-ID PIC 9(09)} */
    @CobolField(name = "XREF-CUST-ID", offset = 16, length = 9, type = PicType.UNSIGNED)
    private Long custId;

    /** {@code XREF-ACCT-ID PIC 9(11)} */
    @CobolField(name = "XREF-ACCT-ID", offset = 25, length = 11, type = PicType.UNSIGNED)
    private Long acctId;

    /** {@code FILLER PIC X(14)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 36, length = 14, type = PicType.ALPHANUMERIC)
    private String filler;

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
