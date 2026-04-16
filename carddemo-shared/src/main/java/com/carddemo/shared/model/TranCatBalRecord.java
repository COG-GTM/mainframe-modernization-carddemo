package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVTRA01Y — Transaction Category Balance (RECLN 50).
 * <pre>
 * 01 TRAN-CAT-BAL-RECORD.
 *   05 TRAN-CAT-KEY.
 *     10 TRANCAT-ACCT-ID          PIC 9(11)
 *     10 TRANCAT-TYPE-CD          PIC X(02)
 *     10 TRANCAT-CD               PIC 9(04)
 *   05 TRAN-CAT-BAL               PIC S9(09)V99
 *   05 FILLER                     PIC X(22)
 * </pre>
 * Composite key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD
 */
public class TranCatBalRecord {

    @JsonProperty("trancatAcctId")
    private long trancatAcctId;

    @JsonProperty("trancatTypeCd")
    private String trancatTypeCd;

    @JsonProperty("trancatCd")
    private int trancatCd;

    @JsonProperty("tranCatBal")
    private BigDecimal tranCatBal;

    public TranCatBalRecord() {
    }

    public long getTrancatAcctId() {
        return trancatAcctId;
    }

    public void setTrancatAcctId(long trancatAcctId) {
        this.trancatAcctId = trancatAcctId;
    }

    public String getTrancatTypeCd() {
        return trancatTypeCd;
    }

    public void setTrancatTypeCd(String trancatTypeCd) {
        this.trancatTypeCd = trancatTypeCd;
    }

    public int getTrancatCd() {
        return trancatCd;
    }

    public void setTrancatCd(int trancatCd) {
        this.trancatCd = trancatCd;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranCatBalRecord that = (TranCatBalRecord) o;
        return trancatAcctId == that.trancatAcctId
                && trancatCd == that.trancatCd
                && Objects.equals(trancatTypeCd, that.trancatTypeCd)
                && Objects.equals(tranCatBal, that.tranCatBal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trancatAcctId, trancatTypeCd, trancatCd, tranCatBal);
    }

    @Override
    public String toString() {
        return "TranCatBalRecord{" +
                "trancatAcctId=" + trancatAcctId +
                ", trancatTypeCd='" + trancatTypeCd + '\'' +
                ", trancatCd=" + trancatCd +
                ", tranCatBal=" + tranCatBal +
                '}';
    }
}
