package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVTRA04Y (TRAN-CAT-RECORD), file TRANCATG, RECLN 60.
 */
@Entity
@Table(name = "tran_category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategory {

    @EmbeddedId
    private TransactionCategoryId id;

    /** TRAN-CAT-TYPE-DESC PIC X(50). */
    @Column(name = "tran_cat_type_desc", length = 50)
    private String description;
}
