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
import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD, RECLN 50).
 * Maintains per-account balances by transaction category.
 * Composite key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD.
 */
@Entity
@Table(name = "transaction_category_balances")
@IdClass(TransactionCategoryBalance.TransactionCategoryBalanceId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryBalance {

    /** TRANCAT-ACCT-ID — PIC 9(11). Account identifier (part of composite key). */
    @Id
    @Column(name = "trancat_acct_id")
    private Long trancatAcctId;

    /** TRANCAT-TYPE-CD — PIC X(02). Transaction type code (part of composite key). */
    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String trancatTypeCd;

    /** TRANCAT-CD — PIC 9(04). Transaction category code (part of composite key). */
    @Id
    @Column(name = "trancat_cd")
    private Integer trancatCd;

    /** TRAN-CAT-BAL — PIC S9(09)V99. Category balance (zoned decimal, 11 bytes). */
    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionCategoryBalanceId implements Serializable {
        private Long trancatAcctId;
        private String trancatTypeCd;
        private Integer trancatCd;
    }
}
