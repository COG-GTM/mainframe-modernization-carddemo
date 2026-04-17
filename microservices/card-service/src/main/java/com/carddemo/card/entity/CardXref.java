package com.carddemo.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CVACT03Y (CARD-XREF-RECORD, RECLN 50).
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", length = 9, nullable = false)
    private String xrefCustId;

    @Column(name = "xref_acct_id", length = 11, nullable = false)
    private String xrefAcctId;

    public CardXref() {
    }

    public CardXref(String xrefCardNum, String xrefCustId, String xrefAcctId) {
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
}
