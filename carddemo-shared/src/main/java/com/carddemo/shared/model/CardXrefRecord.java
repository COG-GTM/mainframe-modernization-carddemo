package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVACT03Y — Card Cross-Reference (RECLN 50).
 * <pre>
 * 01 CARD-XREF-RECORD.
 *   05 XREF-CARD-NUM              PIC X(16)
 *   05 XREF-CUST-ID               PIC 9(09)
 *   05 XREF-ACCT-ID               PIC 9(11)
 *   05 FILLER                     PIC X(14)
 * </pre>
 */
public class CardXrefRecord {

    @JsonProperty("xrefCardNum")
    private String xrefCardNum;

    @JsonProperty("xrefCustId")
    private long xrefCustId;

    @JsonProperty("xrefAcctId")
    private long xrefAcctId;

    public CardXrefRecord() {
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

    public long getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(long xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardXrefRecord that = (CardXrefRecord) o;
        return xrefCustId == that.xrefCustId
                && xrefAcctId == that.xrefAcctId
                && Objects.equals(xrefCardNum, that.xrefCardNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xrefCardNum, xrefCustId, xrefAcctId);
    }

    @Override
    public String toString() {
        return "CardXrefRecord{" +
                "xrefCardNum='" + xrefCardNum + '\'' +
                ", xrefCustId=" + xrefCustId +
                ", xrefAcctId=" + xrefAcctId +
                '}';
    }
}
