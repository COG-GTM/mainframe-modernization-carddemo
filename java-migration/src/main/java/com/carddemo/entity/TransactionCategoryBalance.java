package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA entity representing a Transaction Category Balance record.
 * <p>
 * Migrated from COBOL copybook CVTRA01Y (TRAN-CAT-BAL-RECORD, RECLN 50).
 * Used by batch programs CBACT04C (interest calculation) and CBTRN02C (transaction posting).
 * <p>
 * COBOL record layout:
 * <pre>
 * 01  TRAN-CAT-BAL-RECORD.
 *     05  TRAN-CAT-KEY.
 *        10 TRANCAT-ACCT-ID      PIC 9(11).    Offset 0,  Length 11
 *        10 TRANCAT-TYPE-CD      PIC X(02).    Offset 11, Length 2
 *        10 TRANCAT-CD           PIC 9(04).    Offset 13, Length 4
 *     05  TRAN-CAT-BAL           PIC S9(09)V99. Offset 17, Length 11
 *     05  FILLER                 PIC X(22).    Offset 28, Length 22
 * </pre>
 *
 * @see TransactionCategoryBalanceId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_category_balances")
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    /**
     * Category balance amount.
     * COBOL: TRAN-CAT-BAL PIC S9(09)V99, offset 17, length 11.
     * Signed with 9 integer digits and 2 decimal places.
     */
    @Column(name = "balance", nullable = false, precision = 11, scale = 2)
    private BigDecimal balance;
}
