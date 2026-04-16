package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVTRA02Y — Disclosure Group (RECLN 50).
 * <pre>
 * 01 DIS-GROUP-RECORD.
 *   05 DIS-GROUP-KEY.
 *     10 DIS-ACCT-GROUP-ID        PIC X(10)
 *     10 DIS-TRAN-TYPE-CD         PIC X(02)
 *     10 DIS-TRAN-CAT-CD          PIC 9(04)
 *   05 DIS-INT-RATE               PIC S9(04)V99
 *   05 FILLER                     PIC X(28)
 * </pre>
 */
public class DisGroupRecord {

    @JsonProperty("disAcctGroupId")
    private String disAcctGroupId;

    @JsonProperty("disTranTypeCd")
    private String disTranTypeCd;

    @JsonProperty("disTranCatCd")
    private int disTranCatCd;

    @JsonProperty("disIntRate")
    private BigDecimal disIntRate;

    public DisGroupRecord() {
    }

    public String getDisAcctGroupId() {
        return disAcctGroupId;
    }

    public void setDisAcctGroupId(String disAcctGroupId) {
        this.disAcctGroupId = disAcctGroupId;
    }

    public String getDisTranTypeCd() {
        return disTranTypeCd;
    }

    public void setDisTranTypeCd(String disTranTypeCd) {
        this.disTranTypeCd = disTranTypeCd;
    }

    public int getDisTranCatCd() {
        return disTranCatCd;
    }

    public void setDisTranCatCd(int disTranCatCd) {
        this.disTranCatCd = disTranCatCd;
    }

    public BigDecimal getDisIntRate() {
        return disIntRate;
    }

    public void setDisIntRate(BigDecimal disIntRate) {
        this.disIntRate = disIntRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisGroupRecord that = (DisGroupRecord) o;
        return disTranCatCd == that.disTranCatCd
                && Objects.equals(disAcctGroupId, that.disAcctGroupId)
                && Objects.equals(disTranTypeCd, that.disTranTypeCd)
                && Objects.equals(disIntRate, that.disIntRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disAcctGroupId, disTranTypeCd, disTranCatCd, disIntRate);
    }

    @Override
    public String toString() {
        return "DisGroupRecord{" +
                "disAcctGroupId='" + disAcctGroupId + '\'' +
                ", disTranTypeCd='" + disTranTypeCd + '\'' +
                ", disTranCatCd=" + disTranCatCd +
                ", disIntRate=" + disIntRate +
                '}';
    }
}
