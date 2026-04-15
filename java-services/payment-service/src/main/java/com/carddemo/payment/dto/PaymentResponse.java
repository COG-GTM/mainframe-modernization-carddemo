package com.carddemo.payment.dto;

import java.math.BigDecimal;

public class PaymentResponse {

    private String transactionId;
    private Long accountId;
    private BigDecimal amountPaid;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(String transactionId, Long accountId, BigDecimal amountPaid,
                           BigDecimal previousBalance, BigDecimal newBalance, String message) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amountPaid = amountPaid;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
