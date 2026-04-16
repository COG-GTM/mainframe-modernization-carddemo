package com.cardemo.batch.model;

/**
 * Maps to CVACT03Y.cpy CARD-XREF-RECORD (50 bytes).
 * Cross-reference for card number to account ID lookup.
 */
public class CardXrefRecord {

    private String xrefCardNum;  // PIC X(16)
    private long xrefCustId;     // PIC 9(09)
    private String xrefAcctId;   // PIC 9(11) — stored as String for report formatting

    public CardXrefRecord() {
    }

    public CardXrefRecord(String xrefCardNum, long xrefCustId, String xrefAcctId) {
        this.xrefCardNum = xrefCardNum;
        this.xrefCustId = xrefCustId;
        this.xrefAcctId = xrefAcctId;
    }

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public void setXrefCardNum(String xrefCardNum) {
        this.xrefCardNum = xrefCardNum;
    }

    public long getXrefCustId() {
        return xrefCustId;
    }

    public void setXrefCustId(long xrefCustId) {
        this.xrefCustId = xrefCustId;
    }

    public String getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(String xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }
}
