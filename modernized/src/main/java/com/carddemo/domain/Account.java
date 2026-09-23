package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** CVACT01Y - account master record (300 bytes). */
@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column(name = "acct_id", nullable = false)
    private Long id;

    @Column(name = "acct_active_status", length = 1)
    private String activeStatus;

    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit = BigDecimal.ZERO;

    @Column(name = "acct_open_date", length = 10)
    private String openDate;

    @Column(name = "acct_expiraion_date", length = 10)
    private String expirationDate;

    @Column(name = "acct_reissue_date", length = 10)
    private String reissueDate;

    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currentCycleCredit = BigDecimal.ZERO;

    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currentCycleDebit = BigDecimal.ZERO;

    @Column(name = "acct_addr_zip", length = 10)
    private String addressZip;

    @Column(name = "acct_group_id", length = 10)
    private String groupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
