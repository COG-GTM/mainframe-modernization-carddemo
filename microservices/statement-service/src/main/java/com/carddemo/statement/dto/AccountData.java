package com.carddemo.statement.dto;

import java.math.BigDecimal;

/**
 * DTO representing account data from the Account Service.
 * Derived from ACCOUNT-RECORD (CVACT01Y.cpy, RECLN 300):
 *   ACCT-ID              PIC 9(11)
 *   ACCT-ACTIVE-STATUS   PIC X(01)
 *   ACCT-CURR-BAL        PIC S9(10)V99
 *   ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   ACCT-OPEN-DATE       PIC X(10)
 *   ACCT-EXPIRAION-DATE  PIC X(10)
 *   ACCT-REISSUE-DATE    PIC X(10)
 *   ACCT-CURR-CYC-CREDIT PIC S9(10)V99
 *   ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
 */
public class AccountData {

    private String accountId;
    private String activeStatus;
    private BigDecimal currentBalance;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;
    private String customerName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String stateCode;
    private String countryCode;
    private String zipCode;
    private int ficoScore;

    public AccountData() {
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public int getFicoScore() {
        return ficoScore;
    }

    public void setFicoScore(int ficoScore) {
        this.ficoScore = ficoScore;
    }
}
