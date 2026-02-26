package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CVACT03Y (CARD-XREF-RECORD, 50 bytes).
 *
 * Original VSAM files:
 *   - CCXREF (KSDS, primary key = XREF-CARD-NUM)
 *   - CXACAIX (alternate index path on CCXREF, key = XREF-ACCT-ID)
 */
@Entity
@Table(name = "card_cross_references")
public class CardCrossReference {

    /** XREF-CARD-NUM PIC X(16) */
    @Id
    @Column(name = "card_number", length = 16)
    private String cardNumber;

    /** XREF-CUST-ID PIC 9(09) */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** XREF-ACCT-ID PIC 9(11) */
    @Column(name = "account_id", nullable = false)
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
