package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVTRA01Y (TRAN-CAT-BAL-RECORD), VSAM file TCATBALF, RECLN 50.
 */
@Entity
@Table(name = "tran_cat_bal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    /** TRAN-CAT-BAL PIC S9(09)V99. */
    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal balance;
}
