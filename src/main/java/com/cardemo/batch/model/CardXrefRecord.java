package com.cardemo.batch.model;

/**
 * Domain model corresponding to CVACT03Y.cpy CARD-XREF-RECORD (50 bytes).
 */
public class CardXrefRecord {

    private String xrefCardNum;   // PIC X(16)
    private String xrefCustId;    // PIC 9(09)
    private String xrefAcctId;    // PIC 9(11)

    public CardXrefRecord() {
    }

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public void setXrefCardNum(String xrefCardNum) {
        this.xrefCardNum = xrefCardNum;
    }

    public String getXrefCustId() {
        return xrefCustId;
    }

    public void setXrefCustId(String xrefCustId) {
        this.xrefCustId = xrefCustId;
    }

    public String getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(String xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }

    @Override
    public String toString() {
        return "CardXrefRecord{" +
                "xrefCardNum='" + xrefCardNum + '\'' +
                ", xrefCustId='" + xrefCustId + '\'' +
                ", xrefAcctId='" + xrefAcctId + '\'' +
                '}';
    }
}
