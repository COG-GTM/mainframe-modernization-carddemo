package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity mapping the TRANSACT VSAM file record layout.
 *
 * COBOL Traceability: Maps CVTRA05Y.cpy TRAN-RECORD (RECLN = 350).
 * <pre>
 *   05 TRAN-ID             PIC X(16)      -> transactionId VARCHAR(16)
 *   05 TRAN-TYPE-CD        PIC X(02)      -> typeCode VARCHAR(2)
 *   05 TRAN-CAT-CD         PIC 9(04)      -> categoryCode INT
 *   05 TRAN-SOURCE         PIC X(10)      -> source VARCHAR(10)
 *   05 TRAN-DESC           PIC X(100)     -> description VARCHAR(100)
 *   05 TRAN-AMT            PIC S9(09)V99  -> amount NUMERIC(11,2)
 *   05 TRAN-MERCHANT-ID    PIC 9(09)      -> merchantId BIGINT
 *   05 TRAN-MERCHANT-NAME  PIC X(50)      -> merchantName VARCHAR(50)
 *   05 TRAN-MERCHANT-CITY  PIC X(50)      -> merchantCity VARCHAR(50)
 *   05 TRAN-MERCHANT-ZIP   PIC X(10)      -> merchantZip VARCHAR(10)
 *   05 TRAN-CARD-NUM       PIC X(16)      -> cardNumber VARCHAR(16)
 *   05 TRAN-ORIG-TS        PIC X(26)      -> originTimestamp TIMESTAMP
 *   05 TRAN-PROC-TS        PIC X(26)      -> processedTimestamp TIMESTAMP
 * </pre>
 */
@Entity
@Table(name = "transaction")
public class TransactionEntity {

    @Id
    @Column(name = "transaction_id", length = 16, nullable = false)
    private String transactionId;

    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "category_code", nullable = false)
    private int categoryCode;

    @Column(name = "source", length = 10)
    private String source;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "amount", precision = 11, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "origin_timestamp")
    private LocalDateTime originTimestamp;

    @Column(name = "processed_timestamp")
    private LocalDateTime processedTimestamp;

    public TransactionEntity() {
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
