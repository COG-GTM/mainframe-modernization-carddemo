package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Java mapping of COBOL copybook COSTM01 — Statement Transaction Record.
 * <pre>
 * 01 TRNX-RECORD.
 *   05 TRNX-KEY.
 *     10 TRNX-CARD-NUM            PIC X(16)
 *     10 TRNX-ID                  PIC X(16)
 *   05 TRNX-REST.
 *     10 TRNX-TYPE-CD             PIC X(02)
 *     10 TRNX-CAT-CD              PIC 9(04)
 *     10 TRNX-SOURCE              PIC X(10)
 *     10 TRNX-DESC                PIC X(100)
 *     10 TRNX-AMT                 PIC S9(09)V99
 *     10 TRNX-MERCHANT-ID         PIC 9(09)
 *     10 TRNX-MERCHANT-NAME       PIC X(50)
 *     10 TRNX-MERCHANT-CITY       PIC X(50)
 *     10 TRNX-MERCHANT-ZIP        PIC X(10)
 *     10 TRNX-ORIG-TS             PIC X(26)
 *     10 TRNX-PROC-TS             PIC X(26)
 *     10 FILLER                   PIC X(20)
 * </pre>
 * Composite key: TRNX-CARD-NUM + TRNX-ID
 */
public class StatementTransactionRecord {

    // Key fields
    @JsonProperty("trnxCardNum")
    private String trnxCardNum;

    @JsonProperty("trnxId")
    private String trnxId;

    // Rest fields
    @JsonProperty("trnxTypeCd")
    private String trnxTypeCd;

    @JsonProperty("trnxCatCd")
    private int trnxCatCd;

    @JsonProperty("trnxSource")
    private String trnxSource;

    @JsonProperty("trnxDesc")
    private String trnxDesc;

    @JsonProperty("trnxAmt")
    private BigDecimal trnxAmt;

    @JsonProperty("trnxMerchantId")
    private long trnxMerchantId;

    @JsonProperty("trnxMerchantName")
    private String trnxMerchantName;

    @JsonProperty("trnxMerchantCity")
    private String trnxMerchantCity;

    @JsonProperty("trnxMerchantZip")
    private String trnxMerchantZip;

    @JsonProperty("trnxOrigTs")
    private String trnxOrigTs;

    @JsonProperty("trnxProcTs")
    private String trnxProcTs;

    public StatementTransactionRecord() {
    }

    public String getTrnxCardNum() {
        return trnxCardNum;
    }

    public void setTrnxCardNum(String trnxCardNum) {
        this.trnxCardNum = trnxCardNum;
    }

    public String getTrnxId() {
        return trnxId;
    }

    public void setTrnxId(String trnxId) {
        this.trnxId = trnxId;
    }

    public String getTrnxTypeCd() {
        return trnxTypeCd;
    }

    public void setTrnxTypeCd(String trnxTypeCd) {
        this.trnxTypeCd = trnxTypeCd;
    }

    public int getTrnxCatCd() {
        return trnxCatCd;
    }

    public void setTrnxCatCd(int trnxCatCd) {
        this.trnxCatCd = trnxCatCd;
    }

    public String getTrnxSource() {
        return trnxSource;
    }

    public void setTrnxSource(String trnxSource) {
        this.trnxSource = trnxSource;
    }

    public String getTrnxDesc() {
        return trnxDesc;
    }

    public void setTrnxDesc(String trnxDesc) {
        this.trnxDesc = trnxDesc;
    }

    public BigDecimal getTrnxAmt() {
        return trnxAmt;
    }

    public void setTrnxAmt(BigDecimal trnxAmt) {
        this.trnxAmt = trnxAmt;
    }

    public long getTrnxMerchantId() {
        return trnxMerchantId;
    }

    public void setTrnxMerchantId(long trnxMerchantId) {
        this.trnxMerchantId = trnxMerchantId;
    }

    public String getTrnxMerchantName() {
        return trnxMerchantName;
    }

    public void setTrnxMerchantName(String trnxMerchantName) {
        this.trnxMerchantName = trnxMerchantName;
    }

    public String getTrnxMerchantCity() {
        return trnxMerchantCity;
    }

    public void setTrnxMerchantCity(String trnxMerchantCity) {
        this.trnxMerchantCity = trnxMerchantCity;
    }

    public String getTrnxMerchantZip() {
        return trnxMerchantZip;
    }

    public void setTrnxMerchantZip(String trnxMerchantZip) {
        this.trnxMerchantZip = trnxMerchantZip;
    }

    public String getTrnxOrigTs() {
        return trnxOrigTs;
    }

    public void setTrnxOrigTs(String trnxOrigTs) {
        this.trnxOrigTs = trnxOrigTs;
    }

    public String getTrnxProcTs() {
        return trnxProcTs;
    }

    public void setTrnxProcTs(String trnxProcTs) {
        this.trnxProcTs = trnxProcTs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatementTransactionRecord that = (StatementTransactionRecord) o;
        return trnxCatCd == that.trnxCatCd
                && trnxMerchantId == that.trnxMerchantId
                && Objects.equals(trnxCardNum, that.trnxCardNum)
                && Objects.equals(trnxId, that.trnxId)
                && Objects.equals(trnxTypeCd, that.trnxTypeCd)
                && Objects.equals(trnxSource, that.trnxSource)
                && Objects.equals(trnxDesc, that.trnxDesc)
                && Objects.equals(trnxAmt, that.trnxAmt)
                && Objects.equals(trnxMerchantName, that.trnxMerchantName)
                && Objects.equals(trnxMerchantCity, that.trnxMerchantCity)
                && Objects.equals(trnxMerchantZip, that.trnxMerchantZip)
                && Objects.equals(trnxOrigTs, that.trnxOrigTs)
                && Objects.equals(trnxProcTs, that.trnxProcTs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trnxCardNum, trnxId, trnxTypeCd, trnxCatCd, trnxSource,
                trnxDesc, trnxAmt, trnxMerchantId, trnxMerchantName,
                trnxMerchantCity, trnxMerchantZip, trnxOrigTs, trnxProcTs);
    }

    @Override
    public String toString() {
        return "StatementTransactionRecord{" +
                "trnxCardNum='" + trnxCardNum + '\'' +
                ", trnxId='" + trnxId + '\'' +
                ", trnxTypeCd='" + trnxTypeCd + '\'' +
                ", trnxCatCd=" + trnxCatCd +
                ", trnxSource='" + trnxSource + '\'' +
                ", trnxDesc='" + trnxDesc + '\'' +
                ", trnxAmt=" + trnxAmt +
                ", trnxMerchantId=" + trnxMerchantId +
                ", trnxMerchantName='" + trnxMerchantName + '\'' +
                ", trnxMerchantCity='" + trnxMerchantCity + '\'' +
                ", trnxMerchantZip='" + trnxMerchantZip + '\'' +
                ", trnxOrigTs='" + trnxOrigTs + '\'' +
                ", trnxProcTs='" + trnxProcTs + '\'' +
                '}';
    }
}
