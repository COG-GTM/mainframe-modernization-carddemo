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
 * Represents a credit card account in the CardDemo system.
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /** ACCT-ID — PIC 9(11). Primary key for account. */
    @Id
    @Column(name = "acct_id")
    private Long acctId;

    /** ACCT-ACTIVE-STATUS — PIC X(01). Account active status flag. */
    @Column(name = "acct_active_status", length = 1)
    private String acctActiveStatus;

    /** ACCT-CURR-BAL — PIC S9(10)V99. Current balance (zoned decimal, 12 bytes). */
    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal acctCurrBal;

    /** ACCT-CREDIT-LIMIT — PIC S9(10)V99. Credit limit (zoned decimal, 12 bytes). */
    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCreditLimit;

    /** ACCT-CASH-CREDIT-LIMIT — PIC S9(10)V99. Cash advance credit limit (zoned decimal, 12 bytes). */
    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCashCreditLimit;

    /** ACCT-OPEN-DATE — PIC X(10). Account open date in YYYY-MM-DD format. */
    @Column(name = "acct_open_date")
    private LocalDate acctOpenDate;

    /** ACCT-EXPIRAION-DATE — PIC X(10). Account expiration date in YYYY-MM-DD format. */
    @Column(name = "acct_expiration_date")
    private LocalDate acctExpirationDate;

    /** ACCT-REISSUE-DATE — PIC X(10). Card reissue date in YYYY-MM-DD format. */
    @Column(name = "acct_reissue_date")
    private LocalDate acctReissueDate;

    /** ACCT-CURR-CYC-CREDIT — PIC S9(10)V99. Current cycle credit total (zoned decimal, 12 bytes). */
    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycCredit;

    /** ACCT-CURR-CYC-DEBIT — PIC S9(10)V99. Current cycle debit total (zoned decimal, 12 bytes). */
    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycDebit;

    /** ACCT-ADDR-ZIP — PIC X(10). Account holder ZIP code. */
    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    /** ACCT-GROUP-ID — PIC X(10). Disclosure group identifier. */
    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;
}
