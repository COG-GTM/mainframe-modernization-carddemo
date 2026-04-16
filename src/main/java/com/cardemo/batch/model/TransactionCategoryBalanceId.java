package com.cardemo.batch.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for TransactionCategoryBalance (ACCT-ID + TYPE-CD + CAT-CD).
 */
public class TransactionCategoryBalanceId implements Serializable {

    private long acctId;
    private String typeCd;
    private int catCd;

    public TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(long acctId, String typeCd, int catCd) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
    }

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryBalanceId that = (TransactionCategoryBalanceId) o;
        return acctId == that.acctId
                && catCd == that.catCd
                && Objects.equals(typeCd, that.typeCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, typeCd, catCd);
    }
}
