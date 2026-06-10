package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps to CVACT03Y.cpy (50-byte XREF record). Links a card number to a customer and account.
 */
@Entity
@Table(name = "CARD_XREF")
public class CardXref {

    @Id
    @Column(name = "CARD_NUM", length = 16)
    private String cardNum;

    @Column(name = "CUST_ID")
    private long custId;

    @Column(name = "ACCT_ID")
    private long acctId;

    public CardXref() {
    }

    public CardXref(String cardNum, long custId, long acctId) {
        this.cardNum = cardNum;
        this.custId = custId;
        this.acctId = acctId;
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
