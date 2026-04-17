package com.carddemo.statement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing card cross-reference data from the Card Service.
 * Derived from CARD-XREF-RECORD (CVACT03Y.cpy, RECLN 50):
 *   XREF-CARD-NUM   PIC X(16)
 *   XREF-CUST-ID    PIC 9(09)
 *   XREF-ACCT-ID    PIC 9(11)
 */
public class CardXrefData {

    @JsonProperty("cardNum")
    private String cardNumber;
    @JsonProperty("custId")
    private String customerId;
    @JsonProperty("acctId")
    private String accountId;

    public CardXrefData() {
    }

    public CardXrefData(String cardNumber, String customerId, String accountId) {
        this.cardNumber = cardNumber;
        this.customerId = customerId;
        this.accountId = accountId;
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
