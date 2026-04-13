package com.cardemo.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for {@link TransactionCategory}.
 * Maps the COBOL TRAN-CAT-KEY group: TRAN-TYPE-CD + TRAN-CAT-CD.
 */
public class TransactionCategoryId implements Serializable {

    private String tranTypeCd;
    private Integer tranCatCd;

    public TransactionCategoryId() {
    }

    public TransactionCategoryId(String tranTypeCd, Integer tranCatCd) {
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryId that = (TransactionCategoryId) o;
        return Objects.equals(tranTypeCd, that.tranTypeCd) && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranTypeCd, tranCatCd);
    }
}
