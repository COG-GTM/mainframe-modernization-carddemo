package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** CVACT03Y - card / account / customer cross reference (50 bytes). */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "xref_cust_id", nullable = false)
    private Long customerId;

    @Column(name = "xref_acct_id", nullable = false)
    private Long accountId;

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
