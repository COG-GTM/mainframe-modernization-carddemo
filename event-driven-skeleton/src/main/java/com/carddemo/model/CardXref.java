package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card cross-reference entity.
 *
 * Maps: CARD-XREF-RECORD from CVACT03Y.cpy (RECLN 50)
 * VSAM KSDS key: XREF-CARD-NUM
 *
 * Links a card number to its owning customer and account,
 * used by CBTRN02C (1500-A-LOOKUP-XREF) for transaction validation
 * and by CBSTM03A (1000-XREFFILE-GET-NEXT) for statement iteration.
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    /** XREF-CARD-NUM PIC X(16) — VSAM primary key */
    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    /** XREF-CUST-ID PIC 9(09) — foreign key to customer */
    @Column(name = "cust_id", nullable = false)
    private Long custId;

    /** XREF-ACCT-ID PIC 9(11) — foreign key to account */
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    protected CardXref() {
    }

    public CardXref(String cardNum, Long custId, Long acctId) {
        this.cardNum = cardNum;
        this.custId = custId;
        this.acctId = acctId;
    }

    public String getCardNum() {
        return cardNum;
    }

    public Long getCustId() {
        return custId;
    }

    public Long getAcctId() {
        return acctId;
    }
}
