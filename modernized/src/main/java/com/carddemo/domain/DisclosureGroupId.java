package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Composite key of CVTRA02Y (account group + transaction type + transaction category). */
@Embeddable
public class DisclosureGroupId implements Serializable {

    @Column(name = "dis_acct_group_id", length = 10, nullable = false)
    private String accountGroupId;

    @Column(name = "dis_tran_type_cd", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "dis_tran_cat_cd", nullable = false)
    private Integer categoryCode;

    protected DisclosureGroupId() {
    }

    public DisclosureGroupId(String accountGroupId, String typeCode, Integer categoryCode) {
        this.accountGroupId = accountGroupId;
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
    }

    public String getAccountGroupId() {
        return accountGroupId;
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
        if (!(other instanceof DisclosureGroupId that)) {
            return false;
        }
        return Objects.equals(accountGroupId, that.accountGroupId)
                && Objects.equals(typeCode, that.typeCode)
                && Objects.equals(categoryCode, that.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountGroupId, typeCode, categoryCode);
    }
}
