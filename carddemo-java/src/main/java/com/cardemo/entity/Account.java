package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD).
 * Total COBOL record length: 300 bytes.
 * Seed data file: acctdata.txt (50 records).
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /** ACCT-ID — PIC 9(11), bytes [0:11]. Primary key. */
    @Id
    @Column(name = "acct_id")
    private Long acctId;

    /** ACCT-ACTIVE-STATUS — PIC X(01), bytes [11:12]. Active flag (Y/N). */
    @Column(name = "acct_active_status", length = 1)
    private String acctActiveStatus;

    /** ACCT-CURR-BAL — PIC S9(10)V99, bytes [12:24]. Current balance (zoned decimal). */
    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal acctCurrBal;

    /** ACCT-CREDIT-LIMIT — PIC S9(10)V99, bytes [24:36]. Credit limit (zoned decimal). */
    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCreditLimit;

    /** ACCT-CASH-CREDIT-LIMIT — PIC S9(10)V99, bytes [36:48]. Cash credit limit (zoned decimal). */
    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCashCreditLimit;

    /** ACCT-OPEN-DATE — PIC X(10), bytes [48:58]. Account open date (YYYY-MM-DD). */
    @Column(name = "acct_open_date")
    private LocalDate acctOpenDate;

    /** ACCT-EXPIRAION-DATE — PIC X(10), bytes [58:68]. Expiration date (YYYY-MM-DD). */
    @Column(name = "acct_expiration_date")
    private LocalDate acctExpirationDate;

    /** ACCT-REISSUE-DATE — PIC X(10), bytes [68:78]. Reissue date (YYYY-MM-DD). */
    @Column(name = "acct_reissue_date")
    private LocalDate acctReissueDate;

    /** ACCT-CURR-CYC-CREDIT — PIC S9(10)V99, bytes [78:90]. Current cycle credit (zoned decimal). */
    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycCredit;

    /** ACCT-CURR-CYC-DEBIT — PIC S9(10)V99, bytes [90:102]. Current cycle debit (zoned decimal). */
    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycDebit;

    /** ACCT-ADDR-ZIP — PIC X(10), bytes [102:112]. Address ZIP code. */
    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    /** ACCT-GROUP-ID — PIC X(10), bytes [112:122]. Account group identifier. */
    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    // FILLER — PIC X(178), bytes [122:300]. Padding — not mapped.
}
