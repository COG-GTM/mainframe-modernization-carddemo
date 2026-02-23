package com.carddemo.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new transaction.
 * Maps to the COTRN2A BMS screen input fields from COTRN02C.
 *
 * Validation rules migrated from VALIDATE-INPUT-KEY-FIELDS
 * and VALIDATE-INPUT-DATA-FIELDS paragraphs.
 */
public class CreateTransactionRequest {

    @Size(max = 11, message = "Account ID must be at most 11 characters")
    @Pattern(regexp = "^$|^\\d+$", message = "Account ID must be numeric")
    private String accountId;

    @Size(max = 16, message = "Card Number must be at most 16 characters")
    @Pattern(regexp = "^$|^\\d+$", message = "Card Number must be numeric")
    private String cardNumber;

    @NotBlank(message = "Type CD can NOT be empty")
    @Pattern(regexp = "\\d{2}", message = "Type CD must be 2 numeric digits")
    private String transactionTypeCd;

    @NotNull(message = "Category CD can NOT be empty")
    private Integer transactionCatCd;

    @NotBlank(message = "Source can NOT be empty")
    @Size(max = 10, message = "Source must be at most 10 characters")
    private String source;

    @NotBlank(message = "Description can NOT be empty")
    @Size(max = 100, message = "Description must be at most 100 characters")
    private String description;

    @NotNull(message = "Amount can NOT be empty")
    @DecimalMin(value = "-99999999.99", message = "Amount minimum is -99999999.99")
    @DecimalMax(value = "99999999.99", message = "Amount maximum is 99999999.99")
    private BigDecimal amount;

    @NotNull(message = "Orig Date can NOT be empty")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate originationDate;

    @NotNull(message = "Proc Date can NOT be empty")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate processingDate;

    @NotNull(message = "Merchant ID can NOT be empty")
    private Integer merchantId;

    @NotBlank(message = "Merchant Name can NOT be empty")
    @Size(max = 50, message = "Merchant Name must be at most 50 characters")
    private String merchantName;

    @NotBlank(message = "Merchant City can NOT be empty")
    @Size(max = 50, message = "Merchant City must be at most 50 characters")
    private String merchantCity;

    @NotBlank(message = "Merchant Zip can NOT be empty")
    @Size(max = 10, message = "Merchant Zip must be at most 10 characters")
    private String merchantZip;

    public CreateTransactionRequest() {
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

    public LocalDate getOriginationDate() {
        return originationDate;
    }

    public void setOriginationDate(LocalDate originationDate) {
        this.originationDate = originationDate;
    }

    public LocalDate getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(LocalDate processingDate) {
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
}
