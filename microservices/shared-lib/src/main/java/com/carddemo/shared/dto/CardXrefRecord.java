package com.carddemo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps to CARD-XREF-RECORD in CVACT03Y.cpy.
 *
 * <pre>
 * 01 CARD-XREF-RECORD.
 *   05 XREF-CARD-NUM              PIC X(16).
 *   05 XREF-CUST-ID               PIC 9(09).
 *   05 XREF-ACCT-ID               PIC 9(11).
 * </pre>
 */
public class CardXrefRecord {

    @JsonProperty("cardNum")
    private String cardNum;

    @JsonProperty("custId")
    private long custId;

    @JsonProperty("acctId")
    private long acctId;

    public CardXrefRecord() {
    }

    public CardXrefRecord(String cardNum, long custId, long acctId) {
        this.cardNum = cardNum;
        this.custId = custId;
        this.acctId = acctId;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
    }
}
