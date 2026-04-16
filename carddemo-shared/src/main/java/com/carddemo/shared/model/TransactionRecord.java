package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVTRA05Y — Transaction Record (RECLN 350).
 * <pre>
 * 01 TRAN-RECORD.
 *   05 TRAN-ID                    PIC X(16)
 *   05 TRAN-TYPE-CD               PIC X(02)
 *   05 TRAN-CAT-CD                PIC 9(04)
 *   05 TRAN-SOURCE                PIC X(10)
 *   05 TRAN-DESC                  PIC X(100)
 *   05 TRAN-AMT                   PIC S9(09)V99
 *   05 TRAN-MERCHANT-ID           PIC 9(09)
 *   05 TRAN-MERCHANT-NAME         PIC X(50)
 *   05 TRAN-MERCHANT-CITY         PIC X(50)
 *   05 TRAN-MERCHANT-ZIP          PIC X(10)
 *   05 TRAN-CARD-NUM              PIC X(16)
 *   05 TRAN-ORIG-TS               PIC X(26)
 *   05 TRAN-PROC-TS               PIC X(26)
 *   05 FILLER                     PIC X(20)
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

    @JsonProperty("tranMerchantId")
    private long tranMerchantId;

    @JsonProperty("tranMerchantName")
    private String tranMerchantName;

    @JsonProperty("tranMerchantCity")
    private String tranMerchantCity;

    @JsonProperty("tranMerchantZip")
    private String tranMerchantZip;

    @JsonProperty("tranCardNum")
    private String tranCardNum;

    @JsonProperty("tranOrigTs")
    private String tranOrigTs;

    @JsonProperty("tranProcTs")
    private String tranProcTs;

    public TransactionRecord() {
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

    public long getTranMerchantId() {
        return tranMerchantId;
    }

    public void setTranMerchantId(long tranMerchantId) {
        this.tranMerchantId = tranMerchantId;
    }

    public String getTranMerchantName() {
        return tranMerchantName;
    }

    public void setTranMerchantName(String tranMerchantName) {
        this.tranMerchantName = tranMerchantName;
    }

    public String getTranMerchantCity() {
        return tranMerchantCity;
    }

    public void setTranMerchantCity(String tranMerchantCity) {
        this.tranMerchantCity = tranMerchantCity;
    }

    public String getTranMerchantZip() {
        return tranMerchantZip;
    }

    public void setTranMerchantZip(String tranMerchantZip) {
        this.tranMerchantZip = tranMerchantZip;
    }

    public String getTranCardNum() {
        return tranCardNum;
    }

    public void setTranCardNum(String tranCardNum) {
        this.tranCardNum = tranCardNum;
    }

    public String getTranOrigTs() {
        return tranOrigTs;
    }

    public void setTranOrigTs(String tranOrigTs) {
        this.tranOrigTs = tranOrigTs;
    }

    public String getTranProcTs() {
        return tranProcTs;
    }

    public void setTranProcTs(String tranProcTs) {
        this.tranProcTs = tranProcTs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRecord that = (TransactionRecord) o;
        return tranCatCd == that.tranCatCd
                && tranMerchantId == that.tranMerchantId
                && Objects.equals(tranId, that.tranId)
                && Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranSource, that.tranSource)
                && Objects.equals(tranDesc, that.tranDesc)
                && Objects.equals(tranAmt, that.tranAmt)
                && Objects.equals(tranMerchantName, that.tranMerchantName)
                && Objects.equals(tranMerchantCity, that.tranMerchantCity)
                && Objects.equals(tranMerchantZip, that.tranMerchantZip)
                && Objects.equals(tranCardNum, that.tranCardNum)
                && Objects.equals(tranOrigTs, that.tranOrigTs)
                && Objects.equals(tranProcTs, that.tranProcTs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranId, tranTypeCd, tranCatCd, tranSource, tranDesc,
                tranAmt, tranMerchantId, tranMerchantName, tranMerchantCity,
                tranMerchantZip, tranCardNum, tranOrigTs, tranProcTs);
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "tranId='" + tranId + '\'' +
                ", tranTypeCd='" + tranTypeCd + '\'' +
                ", tranCatCd=" + tranCatCd +
                ", tranSource='" + tranSource + '\'' +
                ", tranDesc='" + tranDesc + '\'' +
                ", tranAmt=" + tranAmt +
                ", tranMerchantId=" + tranMerchantId +
                ", tranMerchantName='" + tranMerchantName + '\'' +
                ", tranMerchantCity='" + tranMerchantCity + '\'' +
                ", tranMerchantZip='" + tranMerchantZip + '\'' +
                ", tranCardNum='" + tranCardNum + '\'' +
                ", tranOrigTs='" + tranOrigTs + '\'' +
                ", tranProcTs='" + tranProcTs + '\'' +
                '}';
    }
}
