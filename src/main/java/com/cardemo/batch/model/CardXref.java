package com.cardemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps to CVACT03Y.cpy CARD-XREF-RECORD (50 bytes).
 * Card cross-reference linking card numbers to accounts and customers.
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String cardNum;

    @Column(name = "xref_cust_id")
    private long custId;

    @Column(name = "xref_acct_id")
    private long acctId;

    public CardXref() {
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
