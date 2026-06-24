package com.carddemo.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Account entity — modern equivalent of copybook {@code CVACT01Y} (VSAM file
 * {@code ACCTDAT}, record length 300). Field comments show the original COBOL
 * PIC clause so the mapping back to the mainframe layout stays traceable.
 *
 * <p>Numeric money fields ({@code PIC S9(10)V99}) map to {@link BigDecimal} with
 * scale 2 — never floating point — to preserve exact base-10 decimal arithmetic.
 * Identifiers ({@code PIC 9(11)}) map to a zero-padded {@code String} to preserve
 * leading zeros.
 */
@Entity
@Table(name = "account")
public class Account {

    /** ACCT-ID PIC 9(11) — 11-digit key, kept as String to preserve leading zeros. */
    @Id
    @Column(name = "acct_id", length = 11, nullable = false)
    private String acctId;

    /** ACCT-ACTIVE-STATUS PIC X(01) — 'Y' active / 'N' inactive. */
    @Column(name = "active_status", length = 1)
    private String activeStatus;

    /** ACCT-CURR-BAL PIC S9(10)V99. */
    @Column(name = "curr_bal", precision = 12, scale = 2)
    private BigDecimal currentBalance;

    /** ACCT-CREDIT-LIMIT PIC S9(10)V99. */
    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99. */
    @Column(name = "cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    /** ACCT-OPEN-DATE PIC X(10). */
    @Column(name = "open_date", length = 10)
    private String openDate;

    /** ACCT-EXPIRAION-DATE PIC X(10) (original copybook spelling preserved). */
    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    /** ACCT-REISSUE-DATE PIC X(10). */
    @Column(name = "reissue_date", length = 10)
    private String reissueDate;

    /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99. */
    @Column(name = "curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit;

    /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99. */
    @Column(name = "curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit;

    /** ACCT-ADDR-ZIP PIC X(10). */
    @Column(name = "addr_zip", length = 10)
    private String addressZip;

    /** ACCT-GROUP-ID PIC X(10). */
    @Column(name = "group_id", length = 10)
    private String groupId;

    protected Account() {
    }

    public Account(String acctId, String activeStatus, BigDecimal currentBalance, BigDecimal creditLimit,
                   BigDecimal cashCreditLimit, String openDate, String expirationDate, String reissueDate,
                   BigDecimal currentCycleCredit, BigDecimal currentCycleDebit, String addressZip, String groupId) {
        this.acctId = acctId;
        this.activeStatus = activeStatus;
        this.currentBalance = currentBalance;
        this.creditLimit = creditLimit;
        this.cashCreditLimit = cashCreditLimit;
        this.openDate = openDate;
        this.expirationDate = expirationDate;
        this.reissueDate = reissueDate;
        this.currentCycleCredit = currentCycleCredit;
        this.currentCycleDebit = currentCycleDebit;
        this.addressZip = addressZip;
        this.groupId = groupId;
    }

    public String getAcctId() {
        return acctId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public String getOpenDate() {
        return openDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getReissueDate() {
        return reissueDate;
    }

    public BigDecimal getCurrentCycleCredit() {
        return currentCycleCredit;
    }

    public BigDecimal getCurrentCycleDebit() {
        return currentCycleDebit;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public String getGroupId() {
        return groupId;
    }
}
