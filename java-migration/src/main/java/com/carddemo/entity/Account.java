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
 * JPA entity representing a credit card account.
 * <p>
 * Migrated from the COBOL VSAM KSDS file {@code ACCTDAT} whose record layout
 * is defined in copybook {@code CVACT01Y}.  The primary key is the 11-digit
 * account identifier ({@code FD-ACCT-ID PIC 9(11)}).
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /** Account identifier – maps to COBOL {@code ACCT-ID PIC 9(11)}. */
    @Id
    @Column(name = "acct_id")
    private Long acctId;

    /** Active status flag – 'Y' or 'N'. Maps to {@code ACCT-ACTIVE-STATUS PIC X(1)}. */
    @Column(name = "active_status", length = 1)
    private String activeStatus;

    /** Current balance – maps to {@code ACCT-CURR-BAL PIC S9(10)V99}. */
    @Column(name = "curr_bal", precision = 13, scale = 2)
    private BigDecimal currBal;

    /** Credit limit – maps to {@code ACCT-CREDIT-LIMIT PIC S9(10)V99}. */
    @Column(name = "credit_limit", precision = 13, scale = 2)
    private BigDecimal creditLimit;

    /** Cash credit limit – maps to {@code ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99}. */
    @Column(name = "cash_credit_limit", precision = 13, scale = 2)
    private BigDecimal cashCreditLimit;

    /** Account open date – maps to {@code ACCT-OPEN-DATE PIC X(10)} (YYYY-MM-DD). */
    @Column(name = "open_date")
    private LocalDate openDate;

    /** Card expiration date – maps to {@code ACCT-EXPIRAION-DATE PIC X(10)} (YYYY-MM-DD). */
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    /** Card reissue date – maps to {@code ACCT-REISSUE-DATE PIC X(10)} (YYYY-MM-DD). */
    @Column(name = "reissue_date")
    private LocalDate reissueDate;

    /** Current cycle credit total – maps to {@code ACCT-CURR-CYC-CREDIT PIC S9(10)V99}. */
    @Column(name = "curr_cyc_credit", precision = 13, scale = 2)
    private BigDecimal currCycCredit;

    /** Current cycle debit total – maps to {@code ACCT-CURR-CYC-DEBIT PIC S9(10)V99}. */
    @Column(name = "curr_cyc_debit", precision = 13, scale = 2)
    private BigDecimal currCycDebit;

    /** Address ZIP code – maps to {@code ACCT-ADDR-ZIP PIC X(10)}. */
    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    /** Group identifier – maps to {@code ACCT-GROUP-ID PIC X(10)}. */
    @Column(name = "group_id", length = 10)
    private String groupId;
}
