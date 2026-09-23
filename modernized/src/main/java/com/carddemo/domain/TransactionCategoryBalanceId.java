package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Composite key of CVTRA01Y (account + transaction type + transaction category). */
@Embeddable
public class TransactionCategoryBalanceId implements Serializable {

    @Column(name = "trancat_acct_id", nullable = false)
    private Long accountId;

    @Column(name = "trancat_type_cd", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "trancat_cd", nullable = false)
    private Integer categoryCode;

    protected TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(Long accountId, String typeCode, Integer categoryCode) {
        this.accountId = accountId;
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public Integer getCategoryCode() {
        return categoryCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TransactionCategoryBalanceId that)) {
            return false;
        }
        return Objects.equals(accountId, that.accountId)
                && Objects.equals(typeCode, that.typeCode)
                && Objects.equals(categoryCode, that.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, typeCode, categoryCode);
    }
}
