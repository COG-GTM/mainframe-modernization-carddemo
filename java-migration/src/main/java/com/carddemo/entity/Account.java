package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD, RECLN 300).
 *
 * <p>Each field documents its original COBOL field name, PIC clause, byte offset, and length.
 * The FILLER field (offset 122, 178 bytes) is intentionally not mapped.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

    /**
     * ACCT-ID — PIC 9(11). Offset 0, Length 11.
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * ACCT-ACTIVE-STATUS — PIC X(01). Offset 11, Length 1.
     */
    @Column(name = "active_status", length = 1, nullable = false)
    private String activeStatus;

    /**
     * ACCT-CURR-BAL — PIC S9(10)V99. Offset 12, Length 12.
     */
    @Column(name = "current_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal currentBalance;

    /**
     * ACCT-CREDIT-LIMIT — PIC S9(10)V99. Offset 24, Length 12.
     */
    @Column(name = "credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal creditLimit;

    /**
     * ACCT-CASH-CREDIT-LIMIT — PIC S9(10)V99. Offset 36, Length 12.
     */
    @Column(name = "cash_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal cashCreditLimit;

    /**
     * ACCT-OPEN-DATE — PIC X(10). Offset 48, Length 10.
     */
    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    /**
     * ACCT-EXPIRAION-DATE — PIC X(10). Offset 58, Length 10.
     * Note: the misspelling of "EXPIRAION" is preserved from the original COBOL copybook.
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * ACCT-REISSUE-DATE — PIC X(10). Offset 68, Length 10.
     */
    @Column(name = "reissue_date", nullable = false)
    private LocalDate reissueDate;

    /**
     * ACCT-CURR-CYC-CREDIT — PIC S9(10)V99. Offset 78, Length 12.
     */
    @Column(name = "current_cycle_credit", precision = 12, scale = 2, nullable = false)
    private BigDecimal currentCycleCredit;

    /**
     * ACCT-CURR-CYC-DEBIT — PIC S9(10)V99. Offset 90, Length 12.
     */
    @Column(name = "current_cycle_debit", precision = 12, scale = 2, nullable = false)
    private BigDecimal currentCycleDebit;

    /**
     * ACCT-ADDR-ZIP — PIC X(10). Offset 102, Length 10.
     */
    @Column(name = "address_zip", length = 10)
    private String addressZip;

    /**
     * ACCT-GROUP-ID — PIC X(10). Offset 112, Length 10.
     */
    @Column(name = "group_id", length = 10)
    private String groupId;
}
