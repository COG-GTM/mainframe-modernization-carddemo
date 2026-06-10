package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Maps to CVACT01Y.cpy (300-byte account record).
 */
@Entity
@Table(name = "ACCOUNT")
public class Account {

    @Id
    @Column(name = "ACCT_ID")
    private long acctId;

    @Column(name = "ACTIVE_STATUS", length = 1)
    private String activeStatus;

    @Column(name = "CURR_BAL", precision = 12, scale = 2)
    private BigDecimal currBal;

    @Column(name = "CREDIT_LIMIT", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "CASH_CREDIT_LIMIT", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    @Column(name = "OPEN_DATE")
    private LocalDate openDate;

    @Column(name = "EXPIRATION_DATE")
    private LocalDate expirationDate;

    @Column(name = "REISSUE_DATE")
    private LocalDate reissueDate;

    @Column(name = "CURR_CYC_CREDIT", precision = 12, scale = 2)
    private BigDecimal currCycCredit;

    @Column(name = "CURR_CYC_DEBIT", precision = 12, scale = 2)
    private BigDecimal currCycDebit;

    @Column(name = "ADDR_ZIP", length = 10)
    private String addrZip;

    @Column(name = "GROUP_ID", length = 10)
    private String groupId;

    public Account() {
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

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDate getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(LocalDate reissueDate) {
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
