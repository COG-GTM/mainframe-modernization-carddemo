package com.carddemo.report.dto;

import java.math.BigDecimal;

/**
 * Individual transaction line within a statement.
 * Mirrors the detail written by CBSTM03A's 6000-WRITE-TRANS paragraph:
 * TRNX-ID, TRNX-DESC, TRNX-AMT, plus running balance.
 */
public class StatementLineDto {

    private String transactionId;
    private String date;
    private String description;
    private String merchantName;
    private String merchantCity;
    private BigDecimal amount;
    private BigDecimal runningBalance;

    public StatementLineDto() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }
}
