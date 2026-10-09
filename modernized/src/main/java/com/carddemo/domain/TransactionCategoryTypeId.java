package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Composite key of CVTRA04Y (transaction type code + category code). */
@Embeddable
public class TransactionCategoryTypeId implements Serializable {

    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "tran_cat_cd", nullable = false)
    private Integer categoryCode;

    protected TransactionCategoryTypeId() {
    }

    public TransactionCategoryTypeId(String typeCode, Integer categoryCode) {
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
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
        if (!(other instanceof TransactionCategoryTypeId that)) {
            return false;
        }
        return Objects.equals(typeCode, that.typeCode) && Objects.equals(categoryCode, that.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeCode, categoryCode);
    }
}
