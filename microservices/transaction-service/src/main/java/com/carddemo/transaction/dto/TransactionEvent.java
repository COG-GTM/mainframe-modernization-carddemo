package com.carddemo.transaction.dto;

import java.math.BigDecimal;

/**
 * Event DTO published to RabbitMQ when a transaction is posted.
 * Translates the COBOL CBTRN02C balance update logic:
 *   ADD DALYTRAN-AMT TO ACCT-CURR-BAL / REWRITE FD-ACCTFILE-REC
 * into an async event consumed by Account Service.
 */
public class TransactionEvent {

    private String transactionId;
    private String cardNum;
    private String accountId;
    private BigDecimal amount;
    private String timestamp;

    public TransactionEvent() {
    }

    public TransactionEvent(String transactionId, String cardNum, String accountId,
                            BigDecimal amount, String timestamp) {
        this.transactionId = transactionId;
        this.cardNum = cardNum;
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
