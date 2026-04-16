package com.carddemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for TransactionCategoryBalance entity.
 * Maps to COBOL TRAN-CAT-KEY (account ID + type code + category code).
 */
@Embeddable
public class TransactionCategoryBalanceId implements Serializable {

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Column(name = "category_code")
    private int categoryCode;

    public TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(long accountId, String typeCode, int categoryCode) {
        this.accountId = accountId;
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryBalanceId that = (TransactionCategoryBalanceId) o;
        return accountId == that.accountId
                && categoryCode == that.categoryCode
                && Objects.equals(typeCode, that.typeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, typeCode, categoryCode);
    }
}
