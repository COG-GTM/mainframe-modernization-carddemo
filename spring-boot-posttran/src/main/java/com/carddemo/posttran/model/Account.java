package com.carddemo.posttran.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to ACCOUNT-RECORD (CVACT01Y copybook).
 * Represents the account master data from ACCTFILE.
 * COBOL record length: 300 bytes.
 */
@Entity
@Table(name = "accounts")
public class Account {

    /** ACCT-ID PIC 9(11) */
    @Id
    @Column(name = "acct_id", nullable = false)
    private long acctId;

    /** ACCT-ACTIVE-STATUS PIC X(01) */
    @Column(name = "acct_active_status", length = 1)
    private String acctActiveStatus;

    /** ACCT-CURR-BAL PIC S9(10)V99 */
    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal acctCurrBal;

    /** ACCT-CREDIT-LIMIT PIC S9(10)V99 */
    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCreditLimit;

    /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99 */
    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCashCreditLimit;

    /** ACCT-OPEN-DATE PIC X(10) */
    @Column(name = "acct_open_date", length = 10)
    private String acctOpenDate;

    /** ACCT-EXPIRAION-DATE PIC X(10) — note COBOL typo; Java uses correct spelling */
    @Column(name = "acct_expiration_date", length = 10)
    private String acctExpirationDate;

    /** ACCT-REISSUE-DATE PIC X(10) */
    @Column(name = "acct_reissue_date", length = 10)
    private String acctReissueDate;

    /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99 */
    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycCredit;

    /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99 */
    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycDebit;

    /** ACCT-ADDR-ZIP PIC X(10) */
    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    /** ACCT-GROUP-ID PIC X(10) */
    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    public Account() {
    }

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
    }

    public String getAcctActiveStatus() {
        return acctActiveStatus;
    }

    public void setAcctActiveStatus(String acctActiveStatus) {
        this.acctActiveStatus = acctActiveStatus;
    }

    public BigDecimal getAcctCurrBal() {
        return acctCurrBal;
    }

    public void setAcctCurrBal(BigDecimal acctCurrBal) {
        this.acctCurrBal = acctCurrBal;
    }

    public BigDecimal getAcctCreditLimit() {
        return acctCreditLimit;
    }

    public void setAcctCreditLimit(BigDecimal acctCreditLimit) {
        this.acctCreditLimit = acctCreditLimit;
    }

    public BigDecimal getAcctCashCreditLimit() {
        return acctCashCreditLimit;
    }

    public void setAcctCashCreditLimit(BigDecimal acctCashCreditLimit) {
        this.acctCashCreditLimit = acctCashCreditLimit;
    }

    public String getAcctOpenDate() {
        return acctOpenDate;
    }

    public void setAcctOpenDate(String acctOpenDate) {
        this.acctOpenDate = acctOpenDate;
    }

    public String getAcctExpirationDate() {
        return acctExpirationDate;
    }

    public void setAcctExpirationDate(String acctExpirationDate) {
        this.acctExpirationDate = acctExpirationDate;
    }

    public String getAcctReissueDate() {
        return acctReissueDate;
    }

    public void setAcctReissueDate(String acctReissueDate) {
        this.acctReissueDate = acctReissueDate;
    }

    public BigDecimal getAcctCurrCycCredit() {
        return acctCurrCycCredit;
    }

    public void setAcctCurrCycCredit(BigDecimal acctCurrCycCredit) {
        this.acctCurrCycCredit = acctCurrCycCredit;
    }

    public BigDecimal getAcctCurrCycDebit() {
        return acctCurrCycDebit;
    }

    public void setAcctCurrCycDebit(BigDecimal acctCurrCycDebit) {
        this.acctCurrCycDebit = acctCurrCycDebit;
    }

    public String getAcctAddrZip() {
        return acctAddrZip;
    }

    public void setAcctAddrZip(String acctAddrZip) {
        this.acctAddrZip = acctAddrZip;
    }

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }
}
