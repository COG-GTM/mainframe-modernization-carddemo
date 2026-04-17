package com.carddemo.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Account master entity.
 *
 * Maps: ACCOUNT-RECORD from CVACT01Y.cpy (RECLN 300)
 * VSAM KSDS key: ACCT-ID
 *
 * Updated by CBTRN02C (2800-UPDATE-ACCOUNT-REC) during transaction posting:
 *   - ACCT-CURR-BAL += txn amount
 *   - ACCT-CURR-CYC-CREDIT or ACCT-CURR-CYC-DEBIT accumulator updated
 *
 * Read by CBTRN02C (1500-B-LOOKUP-ACCT) for validation:
 *   - Credit limit check (code 102)
 *   - Expiration date check (code 103)
 *
 * Read by CBSTM03A (3000-ACCTFILE-GET) for statement header generation.
 */
@Entity
@Table(name = "account")
public class Account {

    /** ACCT-ID PIC 9(11) — VSAM primary key */
    @Id
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    /** ACCT-ACTIVE-STATUS PIC X(01) */
    @Column(name = "active_status", length = 1)
    private String activeStatus;

    /** ACCT-CURR-BAL PIC S9(10)V99 — current balance */
    @Column(name = "current_balance", precision = 12, scale = 2)
    private BigDecimal currentBalance;

    /** ACCT-CREDIT-LIMIT PIC S9(10)V99 — credit limit for overlimit check */
    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99 */
    @Column(name = "cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    /** ACCT-OPEN-DATE PIC X(10) */
    @Column(name = "open_date", length = 10)
    private String openDate;

    /** ACCT-EXPIRAION-DATE PIC X(10) — checked in validation code 103 */
    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    /** ACCT-REISSUE-DATE PIC X(10) */
    @Column(name = "reissue_date", length = 10)
    private String reissueDate;

    /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99 — credit accumulator for current cycle */
    @Column(name = "current_cycle_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit;

    /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99 — debit accumulator for current cycle */
    @Column(name = "current_cycle_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit;

    /** ACCT-ADDR-ZIP PIC X(10) */
    @Column(name = "addr_zip", length = 10)
    private String addressZip;

    /** ACCT-GROUP-ID PIC X(10) — disclosure/interest group identifier */
    @Column(name = "group_id", length = 10)
    private String groupId;

    protected Account() {
    }

    public Account(Long acctId) {
        this.acctId = acctId;
    }

    public Long getAcctId() {
        return acctId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
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

    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) {
        this.currentCycleCredit = currentCycleCredit;
    }

    public BigDecimal getCurrentCycleDebit() {
        return currentCycleDebit;
    }

    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) {
        this.currentCycleDebit = currentCycleDebit;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public String getGroupId() {
        return groupId;
    }
}
