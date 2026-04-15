package com.carddemo.transaction.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for TransactionCategoryBalance, mapped from COBOL TRAN-CAT-KEY (CVTRA01Y.cpy).
 *
 * COBOL layout:
 *   TRANCAT-ACCT-ID   PIC 9(11)  -> accountId
 *   TRANCAT-TYPE-CD   PIC X(02)  -> typeCode
 *   TRANCAT-CD        PIC 9(04)  -> categoryCode
 */
public class TransactionCategoryBalanceId implements Serializable {

    private long accountId;
    private String typeCode;
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
