package com.carddemo.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity mapping for Card Cross-Reference record.
 * Based on COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD, RECLN 50).
 */
@Entity
@Table(name = "card_cross_references")
public class CardCrossReference {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String cardNumber;

    @Column(name = "xref_cust_id")
    private Long customerId;

    @Column(name = "xref_acct_id")
    private Long accountId;

    public CardCrossReference() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
