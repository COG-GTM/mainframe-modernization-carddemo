package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for {@link TransactionCategory} — maps {@code TRAN-CAT-KEY} of copybook
 * {@code CVTRA04Y}.
 */
@Embeddable
public class TransactionCategoryId implements Serializable {

    /** TRAN-TYPE-CD PIC X(02). */
    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    /** TRAN-CAT-CD PIC 9(04). */
    @Column(name = "tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    public TransactionCategoryId() {
    }

    public TransactionCategoryId(String tranTypeCd, Integer tranCatCd) {
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionCategoryId that)) {
            return false;
        }
        return Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranTypeCd, tranCatCd);
    }
}
