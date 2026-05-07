package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a transaction category type.
 *
 * <p>Migrated from COBOL copybook CVTRA04Y.cpy (RECLN = 60):
 * <pre>
 *   01  TRAN-CAT-RECORD.
 *       05  TRAN-CAT-KEY.
 *          10  TRAN-TYPE-CD        PIC X(02).   -> id.typeCode
 *          10  TRAN-CAT-CD         PIC 9(04).   -> id.categoryCode
 *       05  TRAN-CAT-TYPE-DESC    PIC X(50).   -> description
 *       05  FILLER                PIC X(04).   (not mapped)
 * </pre>
 *
 * <p>Used by COBOL program CBTRN03C.cbl for batch transaction processing.
 *
 * @see TransactionCategoryId
 */
@Entity
@Table(name = "transaction_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategory {

    @EmbeddedId
    private TransactionCategoryId id;

    /**
     * Description of the transaction category type.
     * COBOL field: TRAN-CAT-TYPE-DESC PIC X(50), offset 6, length 50.
     */
    @Column(name = "description", nullable = false, length = 50)
    private String description;
}
