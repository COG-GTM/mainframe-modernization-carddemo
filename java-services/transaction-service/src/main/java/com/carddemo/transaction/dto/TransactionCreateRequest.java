package com.carddemo.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class TransactionCreateRequest {

    @NotBlank(message = "Card number is required")
    @Size(max = 16, message = "Card number must be at most 16 characters")
    private String cardNumber;

    @NotBlank(message = "Type code is required")
    @Size(max = 2, message = "Type code must be at most 2 characters")
    private String typeCode;

    private int categoryCode;

    @Size(max = 10, message = "Source must be at most 10 characters")
    private String source;

    @Size(max = 100, message = "Description must be at most 100 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private int merchantId;

    @Size(max = 50, message = "Merchant name must be at most 50 characters")
    private String merchantName;

    @Size(max = 50, message = "Merchant city must be at most 50 characters")
    private String merchantCity;

    @Size(max = 10, message = "Merchant zip must be at most 10 characters")
    private String merchantZip;

    public TransactionCreateRequest() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
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

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
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
}
