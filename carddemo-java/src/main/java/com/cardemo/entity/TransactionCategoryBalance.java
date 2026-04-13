package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD).
 * Total COBOL record length: 50 bytes.
 * Seed data file: tcatbal.txt.
 * Tracks balance per account per transaction type/category combination.
 * Composite key: (trancatAcctId, trancatTypeCd, trancatCd).
 */
@Entity
@Table(name = "transaction_category_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TransactionCategoryBalanceId.class)
public class TransactionCategoryBalance {

    /** TRANCAT-ACCT-ID — PIC 9(11), bytes [0:11]. Account ID (part of composite key). */
    @Id
    @Column(name = "trancat_acct_id")
    private Long trancatAcctId;

    /** TRANCAT-TYPE-CD — PIC X(02), bytes [11:13]. Transaction type code (part of composite key). */
    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String trancatTypeCd;

    /** TRANCAT-CD — PIC 9(04), bytes [13:17]. Transaction category code (part of composite key). */
    @Id
    @Column(name = "trancat_cd")
    private Integer trancatCd;

    /** TRAN-CAT-BAL — PIC S9(09)V99, bytes [17:28]. Category balance (zoned decimal). */
    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    // FILLER — PIC X(22), bytes [28:50]. Padding — not mapped.
}
