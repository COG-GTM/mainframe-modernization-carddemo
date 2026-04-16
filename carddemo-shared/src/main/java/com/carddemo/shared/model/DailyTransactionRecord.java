package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVTRA06Y — Daily Transaction Record (RECLN 350).
 * Same structure as CVTRA05Y but with DALYTRAN- prefix.
 * <pre>
 * 01 DALYTRAN-RECORD.
 *   05 DALYTRAN-ID                PIC X(16)
 *   05 DALYTRAN-TYPE-CD           PIC X(02)
 *   05 DALYTRAN-CAT-CD            PIC 9(04)
 *   05 DALYTRAN-SOURCE            PIC X(10)
 *   05 DALYTRAN-DESC              PIC X(100)
 *   05 DALYTRAN-AMT               PIC S9(09)V99
 *   05 DALYTRAN-MERCHANT-ID       PIC 9(09)
 *   05 DALYTRAN-MERCHANT-NAME     PIC X(50)
 *   05 DALYTRAN-MERCHANT-CITY     PIC X(50)
 *   05 DALYTRAN-MERCHANT-ZIP      PIC X(10)
 *   05 DALYTRAN-CARD-NUM          PIC X(16)
 *   05 DALYTRAN-ORIG-TS           PIC X(26)
 *   05 DALYTRAN-PROC-TS           PIC X(26)
 *   05 FILLER                     PIC X(20)
 * </pre>
 */
public class DailyTransactionRecord {

    @JsonProperty("dalytranId")
    private String dalytranId;

    @JsonProperty("dalytranTypeCd")
    private String dalytranTypeCd;

    @JsonProperty("dalytranCatCd")
    private int dalytranCatCd;

    @JsonProperty("dalytranSource")
    private String dalytranSource;

    @JsonProperty("dalytranDesc")
    private String dalytranDesc;

    @JsonProperty("dalytranAmt")
    private BigDecimal dalytranAmt;

    @JsonProperty("dalytranMerchantId")
    private long dalytranMerchantId;

    @JsonProperty("dalytranMerchantName")
    private String dalytranMerchantName;

    @JsonProperty("dalytranMerchantCity")
    private String dalytranMerchantCity;

    @JsonProperty("dalytranMerchantZip")
    private String dalytranMerchantZip;

    @JsonProperty("dalytranCardNum")
    private String dalytranCardNum;

    @JsonProperty("dalytranOrigTs")
    private String dalytranOrigTs;

    @JsonProperty("dalytranProcTs")
    private String dalytranProcTs;

    public DailyTransactionRecord() {
    }

    public String getDalytranId() {
        return dalytranId;
    }

    public void setDalytranId(String dalytranId) {
        this.dalytranId = dalytranId;
    }

    public String getDalytranTypeCd() {
        return dalytranTypeCd;
    }

    public void setDalytranTypeCd(String dalytranTypeCd) {
        this.dalytranTypeCd = dalytranTypeCd;
    }

    public int getDalytranCatCd() {
        return dalytranCatCd;
    }

    public void setDalytranCatCd(int dalytranCatCd) {
        this.dalytranCatCd = dalytranCatCd;
    }

    public String getDalytranSource() {
        return dalytranSource;
    }

    public void setDalytranSource(String dalytranSource) {
        this.dalytranSource = dalytranSource;
    }

    public String getDalytranDesc() {
        return dalytranDesc;
    }

    public void setDalytranDesc(String dalytranDesc) {
        this.dalytranDesc = dalytranDesc;
    }

    public BigDecimal getDalytranAmt() {
        return dalytranAmt;
    }

    public void setDalytranAmt(BigDecimal dalytranAmt) {
        this.dalytranAmt = dalytranAmt;
    }

    public long getDalytranMerchantId() {
        return dalytranMerchantId;
    }

    public void setDalytranMerchantId(long dalytranMerchantId) {
        this.dalytranMerchantId = dalytranMerchantId;
    }

    public String getDalytranMerchantName() {
        return dalytranMerchantName;
    }

    public void setDalytranMerchantName(String dalytranMerchantName) {
        this.dalytranMerchantName = dalytranMerchantName;
    }

    public String getDalytranMerchantCity() {
        return dalytranMerchantCity;
    }

    public void setDalytranMerchantCity(String dalytranMerchantCity) {
        this.dalytranMerchantCity = dalytranMerchantCity;
    }

    public String getDalytranMerchantZip() {
        return dalytranMerchantZip;
    }

    public void setDalytranMerchantZip(String dalytranMerchantZip) {
        this.dalytranMerchantZip = dalytranMerchantZip;
    }

    public String getDalytranCardNum() {
        return dalytranCardNum;
    }

    public void setDalytranCardNum(String dalytranCardNum) {
        this.dalytranCardNum = dalytranCardNum;
    }

    public String getDalytranOrigTs() {
        return dalytranOrigTs;
    }

    public void setDalytranOrigTs(String dalytranOrigTs) {
        this.dalytranOrigTs = dalytranOrigTs;
    }

    public String getDalytranProcTs() {
        return dalytranProcTs;
    }

    public void setDalytranProcTs(String dalytranProcTs) {
        this.dalytranProcTs = dalytranProcTs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyTransactionRecord that = (DailyTransactionRecord) o;
        return dalytranCatCd == that.dalytranCatCd
                && dalytranMerchantId == that.dalytranMerchantId
                && Objects.equals(dalytranId, that.dalytranId)
                && Objects.equals(dalytranTypeCd, that.dalytranTypeCd)
                && Objects.equals(dalytranSource, that.dalytranSource)
                && Objects.equals(dalytranDesc, that.dalytranDesc)
                && Objects.equals(dalytranAmt, that.dalytranAmt)
                && Objects.equals(dalytranMerchantName, that.dalytranMerchantName)
                && Objects.equals(dalytranMerchantCity, that.dalytranMerchantCity)
                && Objects.equals(dalytranMerchantZip, that.dalytranMerchantZip)
                && Objects.equals(dalytranCardNum, that.dalytranCardNum)
                && Objects.equals(dalytranOrigTs, that.dalytranOrigTs)
                && Objects.equals(dalytranProcTs, that.dalytranProcTs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dalytranId, dalytranTypeCd, dalytranCatCd, dalytranSource,
                dalytranDesc, dalytranAmt, dalytranMerchantId, dalytranMerchantName,
                dalytranMerchantCity, dalytranMerchantZip, dalytranCardNum,
                dalytranOrigTs, dalytranProcTs);
    }

    @Override
    public String toString() {
        return "DailyTransactionRecord{" +
                "dalytranId='" + dalytranId + '\'' +
                ", dalytranTypeCd='" + dalytranTypeCd + '\'' +
                ", dalytranCatCd=" + dalytranCatCd +
                ", dalytranSource='" + dalytranSource + '\'' +
                ", dalytranDesc='" + dalytranDesc + '\'' +
                ", dalytranAmt=" + dalytranAmt +
                ", dalytranMerchantId=" + dalytranMerchantId +
                ", dalytranMerchantName='" + dalytranMerchantName + '\'' +
                ", dalytranMerchantCity='" + dalytranMerchantCity + '\'' +
                ", dalytranMerchantZip='" + dalytranMerchantZip + '\'' +
                ", dalytranCardNum='" + dalytranCardNum + '\'' +
                ", dalytranOrigTs='" + dalytranOrigTs + '\'' +
                ", dalytranProcTs='" + dalytranProcTs + '\'' +
                '}';
    }
}
