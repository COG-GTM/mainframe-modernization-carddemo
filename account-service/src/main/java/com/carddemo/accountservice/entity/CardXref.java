package com.carddemo.accountservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping CVACT03Y.cpy CARD-XREF-RECORD (50-byte VSAM layout).
 * Cross-references card numbers to customer and account IDs.
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", precision = 9, scale = 0, nullable = false)
    private Long xrefCustId;

    @Column(name = "xref_acct_id", precision = 11, scale = 0, nullable = false)
    private Long xrefAcctId;

    public CardXref() {
    }

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public void setXrefCardNum(String xrefCardNum) {
        this.xrefCardNum = xrefCardNum;
    }

    public Long getXrefCustId() {
        return xrefCustId;
    }

    public void setXrefCustId(Long xrefCustId) {
        this.xrefCustId = xrefCustId;
    }

    public Long getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(Long xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }
}
