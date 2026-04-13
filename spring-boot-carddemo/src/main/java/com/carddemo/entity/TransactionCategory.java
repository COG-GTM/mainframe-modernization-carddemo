package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * JPA entity mapped from COBOL copybook CVTRA04Y.cpy (TRAN-CAT-RECORD, RECLN 60).
 * Reference table for transaction categories within a type.
 * Composite key: TRAN-TYPE-CD + TRAN-CAT-CD.
 */
@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategory.TransactionCategoryId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategory {

    /** TRAN-TYPE-CD — PIC X(02). Transaction type code (part of composite key). */
    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    /** TRAN-CAT-CD — PIC 9(04). Transaction category code (part of composite key). */
    @Id
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    /** TRAN-CAT-TYPE-DESC — PIC X(50). Description of the transaction category. */
    @Column(name = "tran_cat_type_desc", length = 50)
    private String tranCatTypeDesc;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionCategoryId implements Serializable {
        private String tranTypeCd;
        private Integer tranCatCd;
    }
}
