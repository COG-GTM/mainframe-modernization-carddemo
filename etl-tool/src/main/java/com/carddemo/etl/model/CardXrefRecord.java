package com.carddemo.etl.model;

/**
 * Maps to CVACT03Y.cpy - Card cross-reference record (RECLN 50).
 */
public class CardXrefRecord {

    private String xrefCardNum;   // PIC X(16)
    private long xrefCustId;      // PIC 9(09)
    private long xrefAcctId;      // PIC 9(11)

    public String getXrefCardNum() { return xrefCardNum; }
    public void setXrefCardNum(String xrefCardNum) { this.xrefCardNum = xrefCardNum; }

    public long getXrefCustId() { return xrefCustId; }
    public void setXrefCustId(long xrefCustId) { this.xrefCustId = xrefCustId; }

    public long getXrefAcctId() { return xrefAcctId; }
    public void setXrefAcctId(long xrefAcctId) { this.xrefAcctId = xrefAcctId; }
}
