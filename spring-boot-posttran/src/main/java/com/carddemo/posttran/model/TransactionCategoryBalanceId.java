package com.carddemo.posttran.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link TransactionCategoryBalance}.
 * Maps to FD-TRAN-CAT-KEY: (TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD).
 */
public class TransactionCategoryBalanceId implements Serializable {

    private static final long serialVersionUID = 1L;

    private long trancatAcctId;
    private String trancatTypeCd;
    private int trancatCd;

    public TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(long trancatAcctId, String trancatTypeCd, int trancatCd) {
        this.trancatAcctId = trancatAcctId;
        this.trancatTypeCd = trancatTypeCd;
        this.trancatCd = trancatCd;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryBalanceId that = (TransactionCategoryBalanceId) o;
        return trancatAcctId == that.trancatAcctId
                && trancatCd == that.trancatCd
                && Objects.equals(trancatTypeCd, that.trancatTypeCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trancatAcctId, trancatTypeCd, trancatCd);
    }
}
