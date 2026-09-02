package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

import java.math.BigDecimal;

/**
 * ACCOUNT-RECORD, copybook CVACT01Y, LRECL 300 (ACCTDATA / acctdata.txt).
 */
public class Account {

    public static final int RECORD_LENGTH = 300;

    private String accountId;
    private char activeStatus;
    private BigDecimal currentBalance;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;
    private String addressZip;
    private String groupId;

    public static Account parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        Account account = new Account();
        account.accountId = cursor.fixed(11);
        account.activeStatus = cursor.flag();
        account.currentBalance = cursor.signed(12, 2);
        account.creditLimit = cursor.signed(12, 2);
        account.cashCreditLimit = cursor.signed(12, 2);
        account.openDate = cursor.text(10);
        account.expirationDate = cursor.text(10);
        account.reissueDate = cursor.text(10);
        account.currentCycleCredit = cursor.signed(12, 2);
        account.currentCycleDebit = cursor.signed(12, 2);
        account.addressZip = cursor.text(10);
        account.groupId = cursor.text(10);
        return account;
    }

    public String format() {
        return CobolCodec.encodeText(accountId, 11)
                + activeStatus
                + CobolCodec.encodeSigned(currentBalance, 12, 2)
                + CobolCodec.encodeSigned(creditLimit, 12, 2)
                + CobolCodec.encodeSigned(cashCreditLimit, 12, 2)
                + CobolCodec.encodeText(openDate, 10)
                + CobolCodec.encodeText(expirationDate, 10)
                + CobolCodec.encodeText(reissueDate, 10)
                + CobolCodec.encodeSigned(currentCycleCredit, 12, 2)
                + CobolCodec.encodeSigned(currentCycleDebit, 12, 2)
                + CobolCodec.encodeText(addressZip, 10)
                + CobolCodec.encodeText(groupId, 10)
                + " ".repeat(178);
    }

    public boolean isActive() {
        return activeStatus == 'Y';
    }

    /** Available credit = credit limit less the outstanding balance. */
    public BigDecimal availableCredit() {
        return creditLimit.subtract(currentBalance);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public char getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(char activeStatus) {
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

    @Override
    public String toString() {
        return "Account[" + accountId + ", status=" + activeStatus + ", balance=" + currentBalance + "]";
    }
}
