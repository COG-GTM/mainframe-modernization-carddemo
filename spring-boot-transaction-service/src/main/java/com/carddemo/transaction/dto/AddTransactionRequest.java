package com.carddemo.transaction.dto;

import com.carddemo.transaction.validation.AccountOrCard;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for adding a new transaction.
 * Replaces the CICS BMS screen input structure COTRN2AI.
 *
 * Mirrors the validation logic from COBOL paragraphs:
 *   - VALIDATE-INPUT-KEY-FIELDS
 *   - VALIDATE-INPUT-DATA-FIELDS
 */
@AccountOrCard
public class AddTransactionRequest {

    /** ACTIDINI - Account ID (optional if cardNumber provided) */
    private Long accountId;

    /** CARDNINI - Card Number (optional if accountId provided) */
    @Size(max = 16, message = "Card Number must be at most 16 characters")
    @Pattern(regexp = "^[0-9]*$", message = "Card Number must be Numeric")
    private String cardNumber;

    /** TTYPCDI - Transaction Type Code, must be numeric, 2 chars */
    @NotBlank(message = "Type CD can NOT be empty")
    @Pattern(regexp = "^[0-9]{2}$", message = "Type CD must be a 2-digit numeric code")
    private String typeCode;

    /** TCATCDI - Transaction Category Code, must be numeric */
    @NotNull(message = "Category CD can NOT be empty")
    @Positive(message = "Category CD must be a positive number")
    private Integer categoryCode;

    /** TRNSRCI - Transaction Source */
    @NotBlank(message = "Source can NOT be empty")
    @Size(max = 10, message = "Source must be at most 10 characters")
    private String source;

    /** TDESCI - Transaction Description */
    @NotBlank(message = "Description can NOT be empty")
    @Size(max = 100, message = "Description must be at most 100 characters")
    private String description;

    /** TRNAMTI - Transaction Amount, format -99999999.99 */
    @NotNull(message = "Amount can NOT be empty")
    @DecimalMin(value = "-99999999.99", message = "Amount must be >= -99999999.99")
    @DecimalMax(value = "99999999.99", message = "Amount must be <= 99999999.99")
    @Digits(integer = 8, fraction = 2, message = "Amount should be in format -99999999.99")
    private BigDecimal amount;

    /** TORIGDTI - Origination Date (YYYY-MM-DD) */
    @NotNull(message = "Orig Date can NOT be empty")
    private LocalDate originatedDate;

    /** TPROCDTI - Processing Date (YYYY-MM-DD) */
    @NotNull(message = "Proc Date can NOT be empty")
    private LocalDate processedDate;

    /** MIDI - Merchant ID, must be numeric */
    @NotNull(message = "Merchant ID can NOT be empty")
    @Positive(message = "Merchant ID must be a positive number")
    private Long merchantId;

    /** MNAMEI - Merchant Name */
    @NotBlank(message = "Merchant Name can NOT be empty")
    @Size(max = 50, message = "Merchant Name must be at most 50 characters")
    private String merchantName;

    /** MCITYI - Merchant City */
    @NotBlank(message = "Merchant City can NOT be empty")
    @Size(max = 50, message = "Merchant City must be at most 50 characters")
    private String merchantCity;

    /** MZIPI - Merchant Zip */
    @NotBlank(message = "Merchant Zip can NOT be empty")
    @Size(max = 10, message = "Merchant Zip must be at most 10 characters")
    private String merchantZip;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public LocalDate getOriginatedDate() {
        return originatedDate;
    }

    public void setOriginatedDate(LocalDate originatedDate) {
        this.originatedDate = originatedDate;
    }

    public LocalDate getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDate processedDate) {
        this.processedDate = processedDate;
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
}
