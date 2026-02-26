package com.carddemo.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a full transaction record.
 * Used by GET endpoints (view transaction, get latest).
 */
public class TransactionResponse {

    private Long transactionId;
    private String typeCode;
    private Integer categoryCode;
    private String source;
    private String description;
    private BigDecimal amount;
    private Long merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String cardNumber;
    private LocalDateTime originatedTs;
    private LocalDateTime processedTs;

    public TransactionResponse() {
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(Integer categoryCode) {
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

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
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

    public LocalDateTime getOriginatedTs() {
        return originatedTs;
    }

    public void setOriginatedTs(LocalDateTime originatedTs) {
        this.originatedTs = originatedTs;
    }

    public LocalDateTime getProcessedTs() {
        return processedTs;
    }

    public void setProcessedTs(LocalDateTime processedTs) {
        this.processedTs = processedTs;
    }
}
