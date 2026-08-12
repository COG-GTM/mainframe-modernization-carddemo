package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** COBOL copybook: CVTRA04Y, TRAN-CAT-KEY group of TRAN-CAT-RECORD. */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** TRAN-TYPE-CD PIC X(02). */
    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String typeCode;

    /** TRAN-CAT-CD PIC 9(04). */
    @Column(name = "tran_cat_cd", nullable = false)
    private Integer categoryCode;
}
