package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Account master record.
 *
 * <p>Transpiled from copybook {@code CVACT01Y} ({@code ACCOUNT-RECORD}, 300 bytes), the record of
 * the {@code ACCTFILE} VSAM KSDS. CBACT04C reads it randomly by {@code ACCT-ID}, adds the
 * accumulated interest to {@code ACCT-CURR-BAL}, zeroes the cycle credit/debit fields, and rewrites
 * it (paragraph {@code 1050-UPDATE-ACCOUNT}).
 *
 * <p>All monetary fields ({@code PIC S9(10)V99}) are {@link BigDecimal} with scale 2.
 */
@Entity
@Table(name = "account")
public class Account {

    /** ACCT-ID PIC 9(11). */
    @Id
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    /** ACCT-ACTIVE-STATUS PIC X(01). */
    @Column(name = "active_status", length = 1)
    private String activeStatus;

    /** ACCT-CURR-BAL PIC S9(10)V99. */
    @Column(name = "curr_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal currBal = BigDecimal.ZERO;

    /** ACCT-CREDIT-LIMIT PIC S9(10)V99. */
    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99. */
    @Column(name = "cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit = BigDecimal.ZERO;

    /** ACCT-OPEN-DATE PIC X(10). */
    @Column(name = "open_date", length = 10)
    private String openDate;

    /** ACCT-EXPIRAION-DATE PIC X(10) (copybook spelling preserved). */
    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    /** ACCT-REISSUE-DATE PIC X(10). */
    @Column(name = "reissue_date", length = 10)
    private String reissueDate;

    /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99 - reset to 0 by the interest run. */
    @Column(name = "curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currCycCredit = BigDecimal.ZERO;

    /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99 - reset to 0 by the interest run. */
    @Column(name = "curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currCycDebit = BigDecimal.ZERO;

    /** ACCT-ADDR-ZIP PIC X(10). */
    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    /** ACCT-GROUP-ID PIC X(10) - disclosure-group key part. */
    @Column(name = "group_id", length = 10)
    private String groupId;

    public Account() {
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public BigDecimal getCurrBal() {
        return currBal;
    }

    public void setCurrBal(BigDecimal currBal) {
        this.currBal = currBal;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(BigDecimal cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(String reissueDate) {
        this.reissueDate = reissueDate;
    }

    public BigDecimal getCurrCycCredit() {
        return currCycCredit;
    }

    public void setCurrCycCredit(BigDecimal currCycCredit) {
        this.currCycCredit = currCycCredit;
    }

    public BigDecimal getCurrCycDebit() {
        return currCycDebit;
    }

    public void setCurrCycDebit(BigDecimal currCycDebit) {
        this.currCycDebit = currCycDebit;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
