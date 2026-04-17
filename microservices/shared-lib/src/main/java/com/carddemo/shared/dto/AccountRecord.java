package com.carddemo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Maps to ACCOUNT-RECORD in CVACT01Y.cpy.
 *
 * <pre>
 * 01 ACCOUNT-RECORD.
 *   05 ACCT-ID                    PIC 9(11).
 *   05 ACCT-ACTIVE-STATUS         PIC X(01).
 *   05 ACCT-CURR-BAL              PIC S9(10)V99.
 *   05 ACCT-CREDIT-LIMIT          PIC S9(10)V99.
 *   05 ACCT-CASH-CREDIT-LIMIT     PIC S9(10)V99.
 *   05 ACCT-OPEN-DATE             PIC X(10).
 *   05 ACCT-EXPIRAION-DATE        PIC X(10).
 *   05 ACCT-REISSUE-DATE          PIC X(10).
 *   05 ACCT-CURR-CYC-CREDIT       PIC S9(10)V99.
 *   05 ACCT-CURR-CYC-DEBIT        PIC S9(10)V99.
 *   05 ACCT-ADDR-ZIP              PIC X(10).
 *   05 ACCT-GROUP-ID              PIC X(10).
 * </pre>
 */
public class AccountRecord {

    @JsonProperty("acctId")
    private long acctId;

    @JsonProperty("activeStatus")
    private String activeStatus;

    @JsonProperty("currentBalance")
    private BigDecimal currentBalance;

    @JsonProperty("creditLimit")
    private BigDecimal creditLimit;

    @JsonProperty("cashCreditLimit")
    private BigDecimal cashCreditLimit;

    @JsonProperty("openDate")
    private String openDate;

    @JsonProperty("expirationDate")
    private String expirationDate;

    @JsonProperty("reissueDate")
    private String reissueDate;

    @JsonProperty("currentCycleCredit")
    private BigDecimal currentCycleCredit;

    @JsonProperty("currentCycleDebit")
    private BigDecimal currentCycleDebit;

    @JsonProperty("addressZip")
    private String addressZip;

    @JsonProperty("groupId")
    private String groupId;

    public AccountRecord() {
    }

    public AccountRecord(long acctId, String activeStatus, BigDecimal currentBalance,
                         BigDecimal creditLimit, BigDecimal cashCreditLimit,
                         String openDate, String expirationDate, String reissueDate,
                         BigDecimal currentCycleCredit, BigDecimal currentCycleDebit,
                         String addressZip, String groupId) {
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

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
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
}
