package com.cardemo.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for {@link TransactionCategoryBalance}.
 * Maps the COBOL TRAN-CAT-KEY: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD.
 */
public class TransactionCategoryBalanceId implements Serializable {

    private Long trancatAcctId;
    private String trancatTypeCd;
    private Integer trancatCd;

    public TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(Long trancatAcctId, String trancatTypeCd, Integer trancatCd) {
        this.trancatAcctId = trancatAcctId;
        this.trancatTypeCd = trancatTypeCd;
        this.trancatCd = trancatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryBalanceId that = (TransactionCategoryBalanceId) o;
        return Objects.equals(trancatAcctId, that.trancatAcctId)
                && Objects.equals(trancatTypeCd, that.trancatTypeCd)
                && Objects.equals(trancatCd, that.trancatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trancatAcctId, trancatTypeCd, trancatCd);
    }
}
