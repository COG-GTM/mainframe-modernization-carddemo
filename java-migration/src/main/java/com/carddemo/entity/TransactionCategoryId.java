package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for TransactionCategory entity.
 *
 * <p>Maps to the COBOL composite key defined in CVTRA04Y.cpy:
 * <pre>
 *   05  TRAN-CAT-KEY.
 *      10  TRAN-TYPE-CD    PIC X(02).   -> typeCode
 *      10  TRAN-CAT-CD     PIC 9(04).   -> categoryCode
 * </pre>
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryId implements Serializable {

    /**
     * Transaction type code.
     * COBOL field: TRAN-TYPE-CD PIC X(02), offset 0, length 2.
     */
    @Column(name = "type_code", nullable = false, length = 2)
    private String typeCode;

    /**
     * Transaction category code.
     * COBOL field: TRAN-CAT-CD PIC 9(04), offset 2, length 4.
     */
    @Column(name = "category_code", nullable = false)
    private Integer categoryCode;
}
