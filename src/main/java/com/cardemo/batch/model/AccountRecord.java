package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Domain model corresponding to CVACT01Y.cpy ACCOUNT-RECORD (300 bytes).
 * Maps all 11 display fields from CBACT01C plus ACCT-ADDR-ZIP.
 */
public class AccountRecord {

    private String acctId;                // PIC 9(11)
    private String acctActiveStatus;      // PIC X(01)
    private BigDecimal acctCurrBal;       // PIC S9(10)V99
    private BigDecimal acctCreditLimit;   // PIC S9(10)V99
    private BigDecimal acctCashCreditLimit; // PIC S9(10)V99
    private String acctOpenDate;          // PIC X(10)
    private String acctExpiraionDate;     // PIC X(10) - typo preserved from COBOL
    private String acctReissueDate;       // PIC X(10)
    private BigDecimal acctCurrCycCredit; // PIC S9(10)V99
    private BigDecimal acctCurrCycDebit;  // PIC S9(10)V99
    private String acctAddrZip;           // PIC X(10)
    private String acctGroupId;           // PIC X(10)

    public AccountRecord() {
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
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

    public String getAcctExpiraionDate() {
        return acctExpiraionDate;
    }

    public void setAcctExpiraionDate(String acctExpiraionDate) {
        this.acctExpiraionDate = acctExpiraionDate;
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
    public String toString() {
        return "AccountRecord{" +
                "acctId='" + acctId + '\'' +
                ", acctActiveStatus='" + acctActiveStatus + '\'' +
                ", acctCurrBal=" + acctCurrBal +
                ", acctCreditLimit=" + acctCreditLimit +
                ", acctCashCreditLimit=" + acctCashCreditLimit +
                ", acctOpenDate='" + acctOpenDate + '\'' +
                ", acctExpiraionDate='" + acctExpiraionDate + '\'' +
                ", acctReissueDate='" + acctReissueDate + '\'' +
                ", acctCurrCycCredit=" + acctCurrCycCredit +
                ", acctCurrCycDebit=" + acctCurrCycDebit +
                ", acctGroupId='" + acctGroupId + '\'' +
                '}';
    }
}
