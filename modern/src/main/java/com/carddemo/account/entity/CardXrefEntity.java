package com.carddemo.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the CXACAIX (card-to-account cross-reference) VSAM layout.
 * Migrated from: CVACT03Y.cpy (Card Cross-Reference — RECLN 50)
 * VSAM file: CXACAIX (AIX PATH on CARDXREF)
 */
@Entity
@Table(name = "card_xref")
public class CardXrefEntity {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "cust_id", nullable = false)
    private Long custId;

    @Column(name = "acct_id", nullable = false)
    private Long accountId;

    public CardXrefEntity() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
