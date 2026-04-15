package com.carddemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Transaction category balance entity mapped from COBOL copybook CVTRA01Y (TRAN-CAT-BAL-RECORD).
 * Original COBOL record length: 50 bytes.
 * Composite key: account ID + transaction type code + category code.
 */
@Entity
@Table(name = "tran_cat_bal")
@IdClass(TransactionCategoryBalanceId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategoryBalance {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Id
    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Id
    @Column(name = "cat_cd")
    private Integer catCd;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;
}
