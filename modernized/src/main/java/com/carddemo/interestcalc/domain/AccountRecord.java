package com.carddemo.interestcalc.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Account master record.
 *
 * <p>Copybook: {@code app/cpy/CVACT01Y.cpy} ({@code ACCOUNT-RECORD}, RECLN = 300)
 *
 * <ul>
 *   <li>{@code accountId}        &larr; {@code ACCT-ID                PIC 9(11)}</li>
 *   <li>{@code activeStatus}     &larr; {@code ACCT-ACTIVE-STATUS     PIC X(01)}</li>
 *   <li>{@code currentBalance}   &larr; {@code ACCT-CURR-BAL          PIC S9(10)V99}</li>
 *   <li>{@code creditLimit}      &larr; {@code ACCT-CREDIT-LIMIT      PIC S9(10)V99}</li>
 *   <li>{@code cashCreditLimit}  &larr; {@code ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99}</li>
 *   <li>{@code openDate}         &larr; {@code ACCT-OPEN-DATE         PIC X(10)}</li>
 *   <li>{@code expirationDate}   &larr; {@code ACCT-EXPIRAION-DATE    PIC X(10)}</li>
 *   <li>{@code reissueDate}      &larr; {@code ACCT-REISSUE-DATE      PIC X(10)}</li>
 *   <li>{@code currentCycleCredit} &larr; {@code ACCT-CURR-CYC-CREDIT PIC S9(10)V99}</li>
 *   <li>{@code currentCycleDebit}  &larr; {@code ACCT-CURR-CYC-DEBIT  PIC S9(10)V99}</li>
 *   <li>{@code addressZip}       &larr; {@code ACCT-ADDR-ZIP          PIC X(10)}</li>
 *   <li>{@code groupId}          &larr; {@code ACCT-GROUP-ID          PIC X(10)}</li>
 *   <li>(FILLER PIC X(178) not mapped)</li>
 * </ul>
 *
 * <p>Mutable because CBACT04C paragraph {@code 1050-UPDATE-ACCOUNT} REWRITEs the record.
 */
public class AccountRecord {

    private final long accountId;
    private final String activeStatus;
    private BigDecimal currentBalance;
    private final BigDecimal creditLimit;
    private final BigDecimal cashCreditLimit;
    private final LocalDate openDate;
    private final LocalDate expirationDate;
    private final LocalDate reissueDate;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;
    private final String addressZip;
    private final String groupId;

    public AccountRecord(long accountId, String activeStatus, BigDecimal currentBalance,
                         BigDecimal creditLimit, BigDecimal cashCreditLimit,
                         LocalDate openDate, LocalDate expirationDate, LocalDate reissueDate,
                         BigDecimal currentCycleCredit, BigDecimal currentCycleDebit,
                         String addressZip, String groupId) {
        this.accountId = accountId;
        this.activeStatus = activeStatus;
        this.currentBalance = currentBalance.setScale(2);
        this.creditLimit = creditLimit.setScale(2);
        this.cashCreditLimit = cashCreditLimit.setScale(2);
        this.openDate = openDate;
        this.expirationDate = expirationDate;
        this.reissueDate = reissueDate;
        this.currentCycleCredit = currentCycleCredit.setScale(2);
        this.currentCycleDebit = currentCycleDebit.setScale(2);
        this.addressZip = addressZip;
        this.groupId = groupId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance.setScale(2);
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public LocalDate getReissueDate() {
        return reissueDate;
    }

    public BigDecimal getCurrentCycleCredit() {
        return currentCycleCredit;
    }

    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) {
        this.currentCycleCredit = currentCycleCredit.setScale(2);
    }

    public BigDecimal getCurrentCycleDebit() {
        return currentCycleDebit;
    }

    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) {
        this.currentCycleDebit = currentCycleDebit.setScale(2);
    }

    public String getAddressZip() {
        return addressZip;
    }

    public String getGroupId() {
        return groupId;
    }
}
