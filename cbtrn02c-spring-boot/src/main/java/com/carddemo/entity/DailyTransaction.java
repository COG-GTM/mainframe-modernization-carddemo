package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to CVTRA06Y.cpy (350-byte DALYTRAN record). Daily transaction input record.
 */
@Entity
@Table(name = "DAILY_TRANSACTION")
public class DailyTransaction {

    @Id
    @Column(name = "ID", length = 16)
    private String id;

    @Column(name = "TYPE_CD", length = 2)
    private String typeCd;

    @Column(name = "CAT_CD")
    private int catCd;

    @Column(name = "SOURCE", length = 10)
    private String source;

    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @Column(name = "AMOUNT", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "MERCHANT_ID")
    private long merchantId;

    @Column(name = "MERCHANT_NAME", length = 50)
    private String merchantName;

    @Column(name = "MERCHANT_CITY", length = 50)
    private String merchantCity;

    @Column(name = "MERCHANT_ZIP", length = 10)
    private String merchantZip;

    @Column(name = "CARD_NUM", length = 16)
    private String cardNum;

    @Column(name = "ORIG_TIMESTAMP", length = 26)
    private String origTimestamp;

    @Column(name = "PROC_TIMESTAMP", length = 26)
    private String procTimestamp;

    public DailyTransaction() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public int getCatCd() {
        return catCd;
    }

    public void setCatCd(int catCd) {
        this.catCd = catCd;
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

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
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

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getOrigTimestamp() {
        return origTimestamp;
    }

    public void setOrigTimestamp(String origTimestamp) {
        this.origTimestamp = origTimestamp;
    }

    public String getProcTimestamp() {
        return procTimestamp;
    }

    public void setProcTimestamp(String procTimestamp) {
        this.procTimestamp = procTimestamp;
    }
}
