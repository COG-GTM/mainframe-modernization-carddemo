package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

/**
 * JPA entity stub for the ACCTDATA VSAM file.
 *
 * COBOL Traceability: Maps CVACT01Y.cpy ACCOUNT-RECORD (RECLN = 300).
 * <pre>
 *   05 ACCT-ID                PIC 9(11)       -> accountId VARCHAR(11)
 *   05 ACCT-ACTIVE-STATUS     PIC X(01)       -> activeStatus CHAR(1)
 *   05 ACCT-CURR-BAL          PIC S9(10)V99   -> currentBalance NUMERIC(12,2)
 *   05 ACCT-CREDIT-LIMIT      PIC S9(10)V99   -> creditLimit NUMERIC(12,2)
 *   05 ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99   -> cashCreditLimit NUMERIC(12,2)
 *   05 ACCT-OPEN-DATE         PIC X(10)       -> openDate VARCHAR(10)
 *   05 ACCT-EXPIRAION-DATE    PIC X(10)       -> expirationDate VARCHAR(10)
 *   05 ACCT-REISSUE-DATE      PIC X(10)       -> reissueDate VARCHAR(10)
 *   05 ACCT-CURR-CYC-CREDIT   PIC S9(10)V99   -> currentCycleCredit NUMERIC(12,2)
 *   05 ACCT-CURR-CYC-DEBIT    PIC S9(10)V99   -> currentCycleDebit NUMERIC(12,2)
 *   05 ACCT-ADDR-ZIP          PIC X(10)       -> addressZip VARCHAR(10)
 *   05 ACCT-GROUP-ID          PIC X(10)       -> groupId VARCHAR(10)
 * </pre>
 */
@Entity
@Table(name = "account")
public class AccountEntity {

    @Id
    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    @Column(name = "current_balance", precision = 12, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    @Column(name = "open_date", length = 10)
    private String openDate;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "reissue_date", length = 10)
    private String reissueDate;

    @Column(name = "current_cycle_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit;

    @Column(name = "current_cycle_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit;

    @Column(name = "address_zip", length = 10)
    private String addressZip;

    @Column(name = "group_id", length = 10)
    private String groupId;

    /**
     * Optimistic locking version field.
     * COBOL Traceability: Replaces CICS READ UPDATE record-level locking.
     * Prevents lost updates when concurrent requests modify the same account.
     */
    @Version
    @Column(name = "version")
    private Long version;

    public AccountEntity() {
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
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

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
