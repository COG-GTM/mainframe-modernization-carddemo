package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity for transaction category reference data.
 * Data file: trancatg.txt
 */
@Entity
@Table(name = "transaction_category")
public class TransactionCategory {

    @EmbeddedId
    private TransactionCategoryId id;

    @Column(name = "category_description", length = 50)
    private String categoryDescription;

    public TransactionCategory() {}

    public TransactionCategoryId getId() { return id; }
    public void setId(TransactionCategoryId id) { this.id = id; }
    public String getCategoryDescription() { return categoryDescription; }
    public void setCategoryDescription(String categoryDescription) { this.categoryDescription = categoryDescription; }

    @Embeddable
    public static class TransactionCategoryId implements Serializable {
        @Column(name = "type_cd", length = 2)
        private String typeCd;

        @Column(name = "category_cd")
        private Integer categoryCd;

        public TransactionCategoryId() {}

        public TransactionCategoryId(String typeCd, Integer categoryCd) {
            this.typeCd = typeCd;
            this.categoryCd = categoryCd;
        }

        public String getTypeCd() { return typeCd; }
        public void setTypeCd(String typeCd) { this.typeCd = typeCd; }
        public Integer getCategoryCd() { return categoryCd; }
        public void setCategoryCd(Integer categoryCd) { this.categoryCd = categoryCd; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionCategoryId that = (TransactionCategoryId) o;
            return Objects.equals(typeCd, that.typeCd) && Objects.equals(categoryCd, that.categoryCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeCd, categoryCd);
        }
    }
}
