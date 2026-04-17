package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity mapping the TRANCATG VSAM file record layout.
 *
 * COBOL Traceability: Maps CVTRA04Y.cpy TRAN-CAT-RECORD (RECLN = 60).
 * <pre>
 *   05 TRAN-CAT-KEY.
 *      10 TRAN-TYPE-CD    PIC X(02)  -> typeCode VARCHAR(2)
 *      10 TRAN-CAT-CD     PIC 9(04)  -> categoryCode INT
 *   05 TRAN-CAT-TYPE-DESC PIC X(50)  -> description VARCHAR(50)
 * </pre>
 */
@Entity
@Table(name = "transaction_category")
@IdClass(TransactionCategoryEntity.TransactionCategoryId.class)
public class TransactionCategoryEntity {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Id
    @Column(name = "category_code", nullable = false)
    private int categoryCode;

    @Column(name = "description", length = 50, nullable = false)
    private String description;

    public TransactionCategoryEntity() {
    }

    public TransactionCategoryEntity(String typeCode, int categoryCode, String description) {
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Composite key for transaction_category table.
     * Maps the COBOL composite key TRAN-CAT-KEY (TRAN-TYPE-CD + TRAN-CAT-CD).
     */
    public static class TransactionCategoryId implements Serializable {
        private String typeCode;
        private int categoryCode;

        public TransactionCategoryId() {
        }

        public TransactionCategoryId(String typeCode, int categoryCode) {
            this.typeCode = typeCode;
            this.categoryCode = categoryCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionCategoryId that = (TransactionCategoryId) o;
            return categoryCode == that.categoryCode && Objects.equals(typeCode, that.typeCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeCode, categoryCode);
        }
    }
}
