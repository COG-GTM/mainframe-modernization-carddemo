package com.carddemo.transaction.dto;

import java.math.BigDecimal;

/**
 * DTO for transaction responses.
 * Represents the data displayed on the COTRN2A BMS screen output fields.
 */
public class TransactionResponse {

    private String transactionId;
    private String cardNumber;
    private String transactionTypeCd;
    private Integer transactionCatCd;
    private String source;
    private String description;
    private BigDecimal amount;
    private String originationDate;
    private String processingDate;
    private Integer merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String message;

    public TransactionResponse() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getTransactionTypeCd() {
        return transactionTypeCd;
    }

    public void setTransactionTypeCd(String transactionTypeCd) {
        this.transactionTypeCd = transactionTypeCd;
    }

    public Integer getTransactionCatCd() {
        return transactionCatCd;
    }

    public void setTransactionCatCd(Integer transactionCatCd) {
        this.transactionCatCd = transactionCatCd;
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

    public String getOriginationDate() {
        return originationDate;
    }

    public void setOriginationDate(String originationDate) {
        this.originationDate = originationDate;
    }

    public String getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(String processingDate) {
        this.processingDate = processingDate;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Integer merchantId) {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
