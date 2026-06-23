package com.carddemo.account;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping the COBOL {@code ACCOUNT-RECORD} (copybook
 * {@code app/cpy/CVACT01Y.cpy}, VSAM ACCTDAT KSDS, record length 300).
 *
 * <p>Field mappings preserve the original COBOL field names and PIC clauses for
 * traceability. The 178-byte trailing {@code FILLER} is intentionally not
 * mapped. Money fields use {@link BigDecimal} (never floating point) to preserve
 * the COBOL fixed-point {@code S9(10)V99} semantics.</p>
 */
@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /** COBOL: {@code ACCT-ID PIC 9(11)} — VSAM primary key (offset 0, len 11). */
    @Id
    @Column(name = "acct_id")
    private Long acctId;

    /** COBOL: {@code ACCT-ACTIVE-STATUS PIC X(01)} — Y/N flag (offset 11, len 1). */
    @Column(name = "acct_active_status", length = 1)
    private String acctActiveStatus;

    /** COBOL: {@code ACCT-CURR-BAL PIC S9(10)V99} — current balance (offset 12, len 12). */
    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal acctCurrBal;

    /** COBOL: {@code ACCT-CREDIT-LIMIT PIC S9(10)V99} — credit limit (offset 24, len 12). */
    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCreditLimit;

    /** COBOL: {@code ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99} — cash credit limit (offset 36, len 12). */
    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCashCreditLimit;

    /** COBOL: {@code ACCT-OPEN-DATE PIC X(10)} — YYYY-MM-DD (offset 48, len 10). */
    @Column(name = "acct_open_date")
    private LocalDate acctOpenDate;

    /** COBOL: {@code ACCT-EXPIRAION-DATE PIC X(10)} — YYYY-MM-DD (offset 58, len 10). */
    @Column(name = "acct_expiration_date")
    private LocalDate acctExpirationDate;

    /** COBOL: {@code ACCT-REISSUE-DATE PIC X(10)} — YYYY-MM-DD (offset 68, len 10). */
    @Column(name = "acct_reissue_date")
    private LocalDate acctReissueDate;

    /** COBOL: {@code ACCT-CURR-CYC-CREDIT PIC S9(10)V99} — current cycle credit (offset 78, len 12). */
    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycCredit;

    /** COBOL: {@code ACCT-CURR-CYC-DEBIT PIC S9(10)V99} — current cycle debit (offset 90, len 12). */
    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycDebit;

    /** COBOL: {@code ACCT-ADDR-ZIP PIC X(10)} — address ZIP (offset 102, len 10). */
    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    /** COBOL: {@code ACCT-GROUP-ID PIC X(10)} — account group id (offset 112, len 10). */
    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;
}
