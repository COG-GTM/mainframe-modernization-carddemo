package com.carddemo.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for creating a new transaction.
 * Mirrors COTRN02C.cbl online add-transaction logic.
 */
public class TransactionCreateRequest {

    @NotBlank(message = "Card number is required")
    @Size(max = 16, message = "Card number must be at most 16 characters")
    private String cardNum;

    @NotBlank(message = "Transaction type code is required")
    @Size(max = 2, message = "Type code must be at most 2 characters")
    private String typeCd;

    private Integer categoryCd;

    @Size(max = 10, message = "Source must be at most 10 characters")
    private String source;

    @Size(max = 100, message = "Description must be at most 100 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 9 integer digits and 2 decimal places")
    private BigDecimal amount;

    private Long merchantId;

    @Size(max = 50, message = "Merchant name must be at most 50 characters")
    private String merchantName;

    @Size(max = 50, message = "Merchant city must be at most 50 characters")
    private String merchantCity;

    @Size(max = 10, message = "Merchant ZIP must be at most 10 characters")
    private String merchantZip;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
    public Integer getCategoryCd() { return categoryCd; }
    public void setCategoryCd(Integer categoryCd) { this.categoryCd = categoryCd; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String merchantCity) { this.merchantCity = merchantCity; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String merchantZip) { this.merchantZip = merchantZip; }
}
