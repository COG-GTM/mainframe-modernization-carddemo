package com.cardemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps to CVACT03Y.cpy CARD-XREF-RECORD.
 * Cross-reference: card number -> customer ID + account ID.
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "cust_id", length = 9, nullable = false)
    private String custId;

    @Column(name = "acct_id", length = 11, nullable = false)
    private String acctId;

    public CardXref() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }
}
