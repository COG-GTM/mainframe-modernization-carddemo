package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** CVTRA06Y - unposted daily transaction (350 bytes), the DALYTRAN input of POSTTRAN. */
@Entity
@Table(name = "daily_transaction")
public class DailyTransaction {

    @Id
    @Column(name = "dalytran_id", length = 16, nullable = false)
    private String id;

    @Column(name = "dalytran_type_cd", length = 2)
    private String typeCode;

    @Column(name = "dalytran_cat_cd")
    private Integer categoryCode;

    @Column(name = "dalytran_source", length = 10)
    private String source;

    @Column(name = "dalytran_desc", length = 100)
    private String description;

    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "dalytran_merchant_id")
    private Long merchantId;

    @Column(name = "dalytran_merchant_name", length = 50)
    private String merchantName;

    @Column(name = "dalytran_merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "dalytran_merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "dalytran_card_num", length = 16)
    private String cardNumber;

    @Column(name = "dalytran_orig_ts", length = 26)
    private String originTimestamp;

    @Column(name = "dalytran_proc_ts", length = 26)
    private String processingTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOriginTimestamp() {
        return originTimestamp;
    }

    public void setOriginTimestamp(String originTimestamp) {
        this.originTimestamp = originTimestamp;
    }

    public String getProcessingTimestamp() {
        return processingTimestamp;
    }

    public void setProcessingTimestamp(String processingTimestamp) {
        this.processingTimestamp = processingTimestamp;
    }
}
