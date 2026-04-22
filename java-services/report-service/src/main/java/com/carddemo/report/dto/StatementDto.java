package com.carddemo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Account statement DTO mirroring the output of CBSTM03A.
 * CBSTM03A generates both HTML and text format statements including:
 * customer name/address, account details, current balance, FICO score,
 * transaction summary with individual line items, and total expenditure.
 * In Java, we return structured JSON that a frontend can render.
 */
public class StatementDto {

    private LocalDate statementDate;
    private String accountId;
    private String customerName;
    private String customerAddress;
    private BigDecimal currentBalance;
    private BigDecimal creditLimit;
    private Integer ficoScore;
    private BigDecimal beginningBalance;
    private BigDecimal totalCharges;
    private BigDecimal totalPayments;
    private BigDecimal endingBalance;
    private List<StatementLineDto> transactions = new ArrayList<>();

    public StatementDto() {
    }

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDate statementDate) {
        this.statementDate = statementDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
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

    public Integer getFicoScore() {
        return ficoScore;
    }

    public void setFicoScore(Integer ficoScore) {
        this.ficoScore = ficoScore;
    }

    public BigDecimal getBeginningBalance() {
        return beginningBalance;
    }

    public void setBeginningBalance(BigDecimal beginningBalance) {
        this.beginningBalance = beginningBalance;
    }

    public BigDecimal getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }

    public BigDecimal getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(BigDecimal totalPayments) {
        this.totalPayments = totalPayments;
    }

    public BigDecimal getEndingBalance() {
        return endingBalance;
    }

    public void setEndingBalance(BigDecimal endingBalance) {
        this.endingBalance = endingBalance;
    }

    public List<StatementLineDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<StatementLineDto> transactions) {
        this.transactions = transactions;
    }
}
