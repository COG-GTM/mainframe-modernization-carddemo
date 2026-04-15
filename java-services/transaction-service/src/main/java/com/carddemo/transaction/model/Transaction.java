package com.carddemo.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Transaction entity mapped from COBOL TRAN-RECORD (CVTRA05Y.cpy, 350 bytes).
 *
 * COBOL layout:
 *   TRAN-ID              PIC X(16)       -> transactionId
 *   TRAN-TYPE-CD         PIC X(02)       -> typeCode
 *   TRAN-CAT-CD          PIC 9(04)       -> categoryCode
 *   TRAN-SOURCE          PIC X(10)       -> source
 *   TRAN-DESC            PIC X(100)      -> description
 *   TRAN-AMT             PIC S9(09)V99   -> amount
 *   TRAN-MERCHANT-ID     PIC 9(09)       -> merchantId
 *   TRAN-MERCHANT-NAME   PIC X(50)       -> merchantName
 *   TRAN-MERCHANT-CITY   PIC X(50)       -> merchantCity
 *   TRAN-MERCHANT-ZIP    PIC X(10)       -> merchantZip
 *   TRAN-CARD-NUM        PIC X(16)       -> cardNumber
 *   TRAN-ORIG-TS         PIC X(26)       -> originTimestamp
 *   TRAN-PROC-TS         PIC X(26)       -> processedTimestamp
 *   FILLER               PIC X(20)
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "transaction_id", length = 16, nullable = false)
    private String transactionId;

    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Column(name = "category_code")
    private int categoryCode;

    @Column(name = "source", length = 10)
    private String source;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "merchant_id")
    private int merchantId;

    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "origin_timestamp", length = 26)
    private String originTimestamp;

    @Column(name = "processed_timestamp", length = 26)
    private String processedTimestamp;

    public Transaction() {
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

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getOriginTimestamp() {
        return originTimestamp;
    }

    public void setOriginTimestamp(String originTimestamp) {
        this.originTimestamp = originTimestamp;
    }

    public String getProcessedTimestamp() {
        return processedTimestamp;
    }

    public void setProcessedTimestamp(String processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }
}
