package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity stub for the CARDXREF VSAM file (cross-reference).
 *
 * COBOL Traceability: Maps CVACT03Y.cpy CARD-XREF-RECORD (RECLN = 50).
 * <pre>
 *   05 XREF-CARD-NUM  PIC X(16)  -> cardNumber VARCHAR(16)
 *   05 XREF-CUST-ID   PIC 9(09)  -> customerId VARCHAR(9)
 *   05 XREF-ACCT-ID   PIC 9(11)  -> accountId VARCHAR(11)
 * </pre>
 *
 * Used by COTRN02C (transaction add) and COBIL00C (bill payment) to validate
 * card-account relationships via CXACAIX alternate index.
 */
@Entity
@Table(name = "card_xref")
public class CardXrefEntity {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "customer_id", length = 9)
    private String customerId;

    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;

    public CardXrefEntity() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
