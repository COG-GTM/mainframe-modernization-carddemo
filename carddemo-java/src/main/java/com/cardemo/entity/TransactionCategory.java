package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVTRA04Y.cpy (TRAN-CAT-RECORD).
 * Total COBOL record length: 60 bytes.
 * Seed data file: trancatg.txt.
 * Reference data: maps transaction type + category code to descriptions.
 * Composite key: (tranTypeCd, tranCatCd).
 */
@Entity
@Table(name = "transaction_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TransactionCategoryId.class)
public class TransactionCategory {

    /** TRAN-TYPE-CD — PIC X(02), bytes [0:2]. Transaction type code (part of composite key). */
    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    /** TRAN-CAT-CD — PIC 9(04), bytes [2:6]. Transaction category code (part of composite key). */
    @Id
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    /** TRAN-CAT-TYPE-DESC — PIC X(50), bytes [6:56]. Category description. */
    @Column(name = "tran_cat_type_desc", length = 50)
    private String tranCatTypeDesc;

    // FILLER — PIC X(04), bytes [56:60]. Padding — not mapped.
}
