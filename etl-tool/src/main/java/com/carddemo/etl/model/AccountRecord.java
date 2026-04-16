package com.carddemo.etl.model;

import java.math.BigDecimal;

/**
 * Maps to CVACT01Y.cpy - Account record (RECLN 300).
 */
public class AccountRecord {

    private long acctId;                  // PIC 9(11)
    private String acctActiveStatus;      // PIC X(01)
    private BigDecimal acctCurrBal;       // PIC S9(10)V99
    private BigDecimal acctCreditLimit;   // PIC S9(10)V99
    private BigDecimal acctCashCreditLimit; // PIC S9(10)V99
    private String acctOpenDate;          // PIC X(10)
    private String acctExpirationDate;    // PIC X(10)
    private String acctReissueDate;       // PIC X(10)
    private BigDecimal acctCurrCycCredit; // PIC S9(10)V99
    private BigDecimal acctCurrCycDebit;  // PIC S9(10)V99
    private String acctAddrZip;           // PIC X(10)
    private String acctGroupId;           // PIC X(10)

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public String getAcctActiveStatus() { return acctActiveStatus; }
    public void setAcctActiveStatus(String acctActiveStatus) { this.acctActiveStatus = acctActiveStatus; }

    public BigDecimal getAcctCurrBal() { return acctCurrBal; }
    public void setAcctCurrBal(BigDecimal acctCurrBal) { this.acctCurrBal = acctCurrBal; }

    public BigDecimal getAcctCreditLimit() { return acctCreditLimit; }
    public void setAcctCreditLimit(BigDecimal acctCreditLimit) { this.acctCreditLimit = acctCreditLimit; }

    public BigDecimal getAcctCashCreditLimit() { return acctCashCreditLimit; }
    public void setAcctCashCreditLimit(BigDecimal acctCashCreditLimit) { this.acctCashCreditLimit = acctCashCreditLimit; }

    public String getAcctOpenDate() { return acctOpenDate; }
    public void setAcctOpenDate(String acctOpenDate) { this.acctOpenDate = acctOpenDate; }

    public String getAcctExpirationDate() { return acctExpirationDate; }
    public void setAcctExpirationDate(String acctExpirationDate) { this.acctExpirationDate = acctExpirationDate; }

    public String getAcctReissueDate() { return acctReissueDate; }
    public void setAcctReissueDate(String acctReissueDate) { this.acctReissueDate = acctReissueDate; }

    public BigDecimal getAcctCurrCycCredit() { return acctCurrCycCredit; }
    public void setAcctCurrCycCredit(BigDecimal acctCurrCycCredit) { this.acctCurrCycCredit = acctCurrCycCredit; }

    public BigDecimal getAcctCurrCycDebit() { return acctCurrCycDebit; }
    public void setAcctCurrCycDebit(BigDecimal acctCurrCycDebit) { this.acctCurrCycDebit = acctCurrCycDebit; }

    public String getAcctAddrZip() { return acctAddrZip; }
    public void setAcctAddrZip(String acctAddrZip) { this.acctAddrZip = acctAddrZip; }

    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String acctGroupId) { this.acctGroupId = acctGroupId; }
}
