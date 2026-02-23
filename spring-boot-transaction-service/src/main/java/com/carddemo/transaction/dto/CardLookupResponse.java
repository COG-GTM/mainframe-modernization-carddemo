package com.carddemo.transaction.dto;

/**
 * DTO for card/account cross-reference lookup responses.
 * Replaces the CXACAIX and CCXREF VSAM file lookups.
 */
public class CardLookupResponse {

    private String cardNumber;
    private Long accountId;
    private Long customerId;

    public CardLookupResponse() {
    }

    public CardLookupResponse(String cardNumber, Long accountId, Long customerId) {
        this.cardNumber = cardNumber;
        this.accountId = accountId;
        this.customerId = customerId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
