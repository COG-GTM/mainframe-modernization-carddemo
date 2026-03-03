package com.carddemo.posttran.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps to CARD-XREF-RECORD (CVACT03Y copybook).
 * Card-to-account-to-customer cross-reference.
 * COBOL record length: 50 bytes.
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    /** XREF-CARD-NUM PIC X(16) — primary key */
    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String xrefCardNum;

    /** XREF-CUST-ID PIC 9(09) */
    @Column(name = "xref_cust_id")
    private long xrefCustId;

    /** XREF-ACCT-ID PIC 9(11) */
    @Column(name = "xref_acct_id")
    private long xrefAcctId;

    public CardXref() {
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
}
