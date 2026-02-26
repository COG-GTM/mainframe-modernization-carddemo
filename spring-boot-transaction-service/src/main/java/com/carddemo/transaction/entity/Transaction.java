package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity mapped from COBOL copybook CVTRA05Y (TRAN-RECORD, 350 bytes).
 *
 * Original VSAM file: TRANSACT (KSDS, key = TRAN-ID).
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tran_id_seq")
    @SequenceGenerator(name = "tran_id_seq", sequenceName = "tran_id_seq", allocationSize = 1)
    @Column(name = "transaction_id")
    private Long transactionId;

    /** TRAN-TYPE-CD PIC X(02) */
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    /** TRAN-CAT-CD PIC 9(04) */
    @Column(name = "category_code", nullable = false)
    private Integer categoryCode;

    /** TRAN-SOURCE PIC X(10) */
    @Column(name = "source", length = 10, nullable = false)
    private String source;

    /** TRAN-DESC PIC X(100) */
    @Column(name = "description", length = 100, nullable = false)
    private String description;

    /** TRAN-AMT PIC S9(09)V99 */
    @Column(name = "amount", precision = 11, scale = 2, nullable = false)
    private BigDecimal amount;

    /** TRAN-MERCHANT-ID PIC 9(09) */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /** TRAN-MERCHANT-NAME PIC X(50) */
    @Column(name = "merchant_name", length = 50, nullable = false)
    private String merchantName;

    /** TRAN-MERCHANT-CITY PIC X(50) */
    @Column(name = "merchant_city", length = 50, nullable = false)
    private String merchantCity;

    /** TRAN-MERCHANT-ZIP PIC X(10) */
    @Column(name = "merchant_zip", length = 10, nullable = false)
    private String merchantZip;

    /** TRAN-CARD-NUM PIC X(16) */
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    /** TRAN-ORIG-TS PIC X(26) */
    @Column(name = "originated_ts", nullable = false)
    private LocalDateTime originatedTs;

    /** TRAN-PROC-TS PIC X(26) */
    @Column(name = "processed_ts", nullable = false)
    private LocalDateTime processedTs;

    public Transaction() {
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
