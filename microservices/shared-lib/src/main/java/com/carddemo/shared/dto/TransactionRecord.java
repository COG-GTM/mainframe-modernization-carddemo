package com.carddemo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Maps to TRAN-RECORD in CVTRA05Y.cpy.
 *
 * <pre>
 * 01 TRAN-RECORD.
 *   05 TRAN-ID                    PIC X(16).
 *   05 TRAN-TYPE-CD               PIC X(02).
 *   05 TRAN-CAT-CD                PIC 9(04).
 *   05 TRAN-SOURCE                PIC X(10).
 *   05 TRAN-DESC                  PIC X(100).
 *   05 TRAN-AMT                   PIC S9(09)V99.
 *   05 TRAN-MERCHANT-ID           PIC 9(09).
 *   05 TRAN-MERCHANT-NAME         PIC X(50).
 *   05 TRAN-MERCHANT-CITY         PIC X(50).
 *   05 TRAN-MERCHANT-ZIP          PIC X(10).
 *   05 TRAN-CARD-NUM              PIC X(16).
 *   05 TRAN-ORIG-TS               PIC X(26).
 *   05 TRAN-PROC-TS               PIC X(26).
 * </pre>
 */
public class TransactionRecord {

    @JsonProperty("tranId")
    private String tranId;

    @JsonProperty("tranTypeCd")
    private String tranTypeCd;

    @JsonProperty("tranCatCd")
    private int tranCatCd;

    @JsonProperty("tranSource")
    private String tranSource;

    @JsonProperty("tranDesc")
    private String tranDesc;

    @JsonProperty("tranAmt")
    private BigDecimal tranAmt;

    @JsonProperty("merchantId")
    private long merchantId;

    @JsonProperty("merchantName")
    private String merchantName;

    @JsonProperty("merchantCity")
    private String merchantCity;

    @JsonProperty("merchantZip")
    private String merchantZip;

    @JsonProperty("cardNum")
    private String cardNum;

    @JsonProperty("origTimestamp")
    private String origTimestamp;

    @JsonProperty("procTimestamp")
    private String procTimestamp;

    public TransactionRecord() {
    }

    public TransactionRecord(String tranId, String tranTypeCd, int tranCatCd,
                             String tranSource, String tranDesc, BigDecimal tranAmt,
                             long merchantId, String merchantName, String merchantCity,
                             String merchantZip, String cardNum,
                             String origTimestamp, String procTimestamp) {
        this.tranId = tranId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
        this.tranSource = tranSource;
        this.tranDesc = tranDesc;
        this.tranAmt = tranAmt;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.merchantCity = merchantCity;
        this.merchantZip = merchantZip;
        this.cardNum = cardNum;
        this.origTimestamp = origTimestamp;
        this.procTimestamp = procTimestamp;
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public int getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(int tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public String getTranSource() {
        return tranSource;
    }

    public void setTranSource(String tranSource) {
        this.tranSource = tranSource;
    }

    public String getTranDesc() {
        return tranDesc;
    }

    public void setTranDesc(String tranDesc) {
        this.tranDesc = tranDesc;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
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
