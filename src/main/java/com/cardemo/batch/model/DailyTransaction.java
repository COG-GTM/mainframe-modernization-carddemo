package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to CVTRA06Y.cpy DALYTRAN-RECORD (350 bytes).
 * Input record from the daily transaction file.
 */
public class DailyTransaction {

    private String id;              // DALYTRAN-ID PIC X(16)
    private String typeCd;          // DALYTRAN-TYPE-CD PIC X(02)
    private int catCd;              // DALYTRAN-CAT-CD PIC 9(04)
    private String source;          // DALYTRAN-SOURCE PIC X(10)
    private String description;     // DALYTRAN-DESC PIC X(100)
    private BigDecimal amount;      // DALYTRAN-AMT PIC S9(09)V99
    private long merchantId;        // DALYTRAN-MERCHANT-ID PIC 9(09)
    private String merchantName;    // DALYTRAN-MERCHANT-NAME PIC X(50)
    private String merchantCity;    // DALYTRAN-MERCHANT-CITY PIC X(50)
    private String merchantZip;     // DALYTRAN-MERCHANT-ZIP PIC X(10)
    private String cardNum;         // DALYTRAN-CARD-NUM PIC X(16)
    private String origTimestamp;   // DALYTRAN-ORIG-TS PIC X(26)
    private String procTimestamp;   // DALYTRAN-PROC-TS PIC X(26)

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
