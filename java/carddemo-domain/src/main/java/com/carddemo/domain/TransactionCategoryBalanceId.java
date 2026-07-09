package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for {@link TransactionCategoryBalance} — maps {@code TRAN-CAT-KEY} of
 * copybook {@code CVTRA01Y}.
 */
@Embeddable
public class TransactionCategoryBalanceId implements Serializable {

    /** TRANCAT-ACCT-ID PIC 9(11). */
    @Column(name = "trancat_acct_id", length = 11, nullable = false)
    private String trancatAcctId;

    /** TRANCAT-TYPE-CD PIC X(02). */
    @Column(name = "trancat_type_cd", length = 2, nullable = false)
    private String trancatTypeCd;

    /** TRANCAT-CD PIC 9(04). */
    @Column(name = "trancat_cd", nullable = false)
    private Integer trancatCd;

    public TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(String trancatAcctId, String trancatTypeCd, Integer trancatCd) {
        this.trancatAcctId = trancatAcctId;
        this.trancatTypeCd = trancatTypeCd;
        this.trancatCd = trancatCd;
    }

    public String getTrancatAcctId() {
        return trancatAcctId;
    }

    public void setTrancatAcctId(String trancatAcctId) {
        this.trancatAcctId = trancatAcctId;
    }

    public String getTrancatTypeCd() {
        return trancatTypeCd;
    }

    public void setTrancatTypeCd(String trancatTypeCd) {
        this.trancatTypeCd = trancatTypeCd;
    }

    public Integer getTrancatCd() {
        return trancatCd;
    }

    public void setTrancatCd(Integer trancatCd) {
        this.trancatCd = trancatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionCategoryBalanceId that)) {
            return false;
        }
        return Objects.equals(trancatAcctId, that.trancatAcctId)
                && Objects.equals(trancatTypeCd, that.trancatTypeCd)
                && Objects.equals(trancatCd, that.trancatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trancatAcctId, trancatTypeCd, trancatCd);
    }
}
