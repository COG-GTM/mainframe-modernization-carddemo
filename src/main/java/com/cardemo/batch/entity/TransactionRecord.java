package com.cardemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to COSTM01.CPY TRNX-RECORD.
 * Composite key: TRNX-CARD-NUM + TRNX-ID (sorted order for statements).
 */
@Entity
@Table(name = "transaction_records")
@IdClass(TransactionRecordKey.class)
public class TransactionRecord {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String tranId;

    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Column(name = "cat_cd")
    private int catCd;

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

    @Column(name = "orig_ts", length = 26)
    private String origTimestamp;

    @Column(name = "proc_ts", length = 26)
    private String procTimestamp;

    public TransactionRecord() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
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
