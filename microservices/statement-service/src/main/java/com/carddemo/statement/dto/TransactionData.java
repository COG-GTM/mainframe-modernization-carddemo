package com.carddemo.statement.dto;

import java.math.BigDecimal;

/**
 * DTO representing transaction data from the Transaction Service.
 * Derived from TRNX-RECORD (COSTM01.CPY):
 *   TRNX-CARD-NUM       PIC X(16)
 *   TRNX-ID              PIC X(16)
 *   TRNX-TYPE-CD         PIC X(02)
 *   TRNX-CAT-CD          PIC 9(04)
 *   TRNX-SOURCE          PIC X(10)
 *   TRNX-DESC            PIC X(100)
 *   TRNX-AMT             PIC S9(09)V99
 *   TRNX-MERCHANT-ID     PIC 9(09)
 *   TRNX-MERCHANT-NAME   PIC X(50)
 *   TRNX-MERCHANT-CITY   PIC X(50)
 *   TRNX-MERCHANT-ZIP    PIC X(10)
 *   TRNX-ORIG-TS         PIC X(26)
 *   TRNX-PROC-TS         PIC X(26)
 */
public class TransactionData {

    private String cardNumber;
    private String transactionId;
    private String typeCode;
    private int categoryCode;
    private String source;
    private String description;
    private BigDecimal amount;
    private String merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String originTimestamp;
    private String processTimestamp;

    public TransactionData() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
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

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
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

    public String getOriginTimestamp() {
        return originTimestamp;
    }

    public void setOriginTimestamp(String originTimestamp) {
        this.originTimestamp = originTimestamp;
    }

    public String getProcessTimestamp() {
        return processTimestamp;
    }

    public void setProcessTimestamp(String processTimestamp) {
        this.processTimestamp = processTimestamp;
    }
}
