package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Customer-Account-Card cross reference — maps COBOL copybook {@code CVACT03Y}
 * (CARD-XREF-RECORD, RECLN 50).
 *
 * <p>VSAM KSDS keyed on {@code XREF-CARD-NUM} (see {@code CBACT03C}: RECORD KEY IS
 * FD-XREF-CARD-NUM). Links {@link Card} ↔ {@link Account} ↔ {@link Customer}.</p>
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    /** XREF-CARD-NUM PIC X(16) — FK to card.card_num. */
    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String xrefCardNum;

    /** XREF-CUST-ID PIC 9(09) — FK to customer.cust_id. */
    @Column(name = "xref_cust_id", length = 9)
    private String xrefCustId;

    /** XREF-ACCT-ID PIC 9(11) — FK to account.acct_id. */
    @Column(name = "xref_acct_id", length = 11)
    private String xrefAcctId;

    public CardXref() {
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
