package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a record from the daily transaction input file.
 * Maps to COBOL copybook CVTRA06Y (DALYTRAN-RECORD, 350 bytes).
 */
public class DailyTransaction {

    private String transactionId;       // PIC X(16)
    private String typeCode;            // PIC X(02)
    private int categoryCode;           // PIC 9(04)
    private String source;              // PIC X(10)
    private String description;         // PIC X(100)
    private BigDecimal amount;          // PIC S9(09)V99
    private long merchantId;            // PIC 9(09)
    private String merchantName;        // PIC X(50)
    private String merchantCity;        // PIC X(50)
    private String merchantZip;         // PIC X(10)
    private String cardNumber;          // PIC X(16)
    private LocalDateTime originTimestamp;    // PIC X(26) format YYYY-MM-DD-HH.MM.SS.HH0000
    private LocalDateTime processedTimestamp; // PIC X(26)

    public DailyTransaction() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
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

    public String getMerchantZip() {
        return merchantZip;
    }

    public void setMerchantZip(String merchantZip) {
        this.merchantZip = merchantZip;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public LocalDateTime getOriginTimestamp() {
        return originTimestamp;
    }

    public void setOriginTimestamp(LocalDateTime originTimestamp) {
        this.originTimestamp = originTimestamp;
    }

    public LocalDateTime getProcessedTimestamp() {
        return processedTimestamp;
    }

    public void setProcessedTimestamp(LocalDateTime processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }
}
