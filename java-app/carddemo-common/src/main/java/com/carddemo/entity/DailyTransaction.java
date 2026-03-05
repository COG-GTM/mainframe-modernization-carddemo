package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for daily transaction staging data.
 * Migrated from COBOL copybook: CVTRA06Y.cpy (DALYTRAN-RECORD)
 * VSAM file: DALYTRAN (RECLN 350)
 */
@Entity
@Table(name = "daily_transaction")
public class DailyTransaction {

    @Id
    @Column(name = "transaction_id", length = 16, nullable = false)
    private String transactionId;

    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Column(name = "category_cd")
    private Integer categoryCd;

    @Column(name = "source", length = 10)
    private String source;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "orig_timestamp")
    private LocalDateTime origTimestamp;

    @Column(name = "proc_timestamp")
    private LocalDateTime procTimestamp;

    public DailyTransaction() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
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
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public LocalDateTime getOrigTimestamp() { return origTimestamp; }
    public void setOrigTimestamp(LocalDateTime origTimestamp) { this.origTimestamp = origTimestamp; }
    public LocalDateTime getProcTimestamp() { return procTimestamp; }
    public void setProcTimestamp(LocalDateTime procTimestamp) { this.procTimestamp = procTimestamp; }
}
