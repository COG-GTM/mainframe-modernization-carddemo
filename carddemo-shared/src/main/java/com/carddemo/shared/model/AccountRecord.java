package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVACT01Y — Account Record (RECLN 300).
 * <pre>
 * 01 ACCOUNT-RECORD.
 *   05 ACCT-ID                    PIC 9(11)
 *   05 ACCT-ACTIVE-STATUS         PIC X(01)
 *   05 ACCT-CURR-BAL              PIC S9(10)V99
 *   05 ACCT-CREDIT-LIMIT          PIC S9(10)V99
 *   05 ACCT-CASH-CREDIT-LIMIT     PIC S9(10)V99
 *   05 ACCT-OPEN-DATE             PIC X(10)
 *   05 ACCT-EXPIRAION-DATE        PIC X(10)
 *   05 ACCT-REISSUE-DATE          PIC X(10)
 *   05 ACCT-CURR-CYC-CREDIT       PIC S9(10)V99
 *   05 ACCT-CURR-CYC-DEBIT        PIC S9(10)V99
 *   05 ACCT-ADDR-ZIP              PIC X(10)
 *   05 ACCT-GROUP-ID              PIC X(10)
 *   05 FILLER                     PIC X(178)
 * </pre>
 */
public class AccountRecord {

    @JsonProperty("acctId")
    private long acctId;

    @JsonProperty("acctActiveStatus")
    private String acctActiveStatus;

    @JsonProperty("acctCurrBal")
    private BigDecimal acctCurrBal;

    @JsonProperty("acctCreditLimit")
    private BigDecimal acctCreditLimit;

    @JsonProperty("acctCashCreditLimit")
    private BigDecimal acctCashCreditLimit;

    @JsonProperty("acctOpenDate")
    private String acctOpenDate;

    @JsonProperty("acctExpirationDate")
    private String acctExpirationDate;

    @JsonProperty("acctReissueDate")
    private String acctReissueDate;

    @JsonProperty("acctCurrCycCredit")
    private BigDecimal acctCurrCycCredit;

    @JsonProperty("acctCurrCycDebit")
    private BigDecimal acctCurrCycDebit;

    @JsonProperty("acctAddrZip")
    private String acctAddrZip;

    @JsonProperty("acctGroupId")
    private String acctGroupId;

    public AccountRecord() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountRecord that = (AccountRecord) o;
        return acctId == that.acctId
                && Objects.equals(acctActiveStatus, that.acctActiveStatus)
                && Objects.equals(acctCurrBal, that.acctCurrBal)
                && Objects.equals(acctCreditLimit, that.acctCreditLimit)
                && Objects.equals(acctCashCreditLimit, that.acctCashCreditLimit)
                && Objects.equals(acctOpenDate, that.acctOpenDate)
                && Objects.equals(acctExpirationDate, that.acctExpirationDate)
                && Objects.equals(acctReissueDate, that.acctReissueDate)
                && Objects.equals(acctCurrCycCredit, that.acctCurrCycCredit)
                && Objects.equals(acctCurrCycDebit, that.acctCurrCycDebit)
                && Objects.equals(acctAddrZip, that.acctAddrZip)
                && Objects.equals(acctGroupId, that.acctGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, acctActiveStatus, acctCurrBal, acctCreditLimit,
                acctCashCreditLimit, acctOpenDate, acctExpirationDate, acctReissueDate,
                acctCurrCycCredit, acctCurrCycDebit, acctAddrZip, acctGroupId);
    }

    @Override
    public String toString() {
        return "AccountRecord{" +
                "acctId=" + acctId +
                ", acctActiveStatus='" + acctActiveStatus + '\'' +
                ", acctCurrBal=" + acctCurrBal +
                ", acctCreditLimit=" + acctCreditLimit +
                ", acctCashCreditLimit=" + acctCashCreditLimit +
                ", acctOpenDate='" + acctOpenDate + '\'' +
                ", acctExpirationDate='" + acctExpirationDate + '\'' +
                ", acctReissueDate='" + acctReissueDate + '\'' +
                ", acctCurrCycCredit=" + acctCurrCycCredit +
                ", acctCurrCycDebit=" + acctCurrCycDebit +
                ", acctAddrZip='" + acctAddrZip + '\'' +
                ", acctGroupId='" + acctGroupId + '\'' +
                '}';
    }
}
