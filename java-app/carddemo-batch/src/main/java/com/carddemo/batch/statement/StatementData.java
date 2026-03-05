package com.carddemo.batch.statement;

import com.carddemo.entity.Account;
import com.carddemo.entity.Customer;
import com.carddemo.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data object holding all information needed to generate a statement.
 */
public class StatementData {

    private final Account account;
    private final Customer customer;
    private final List<Transaction> transactions;
    private final BigDecimal totalDebits;
    private final BigDecimal totalCredits;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    public StatementData(Account account, Customer customer, List<Transaction> transactions,
                          BigDecimal totalDebits, BigDecimal totalCredits,
                          LocalDate periodStart, LocalDate periodEnd) {
        this.account = account;
        this.customer = customer;
        this.transactions = transactions;
        this.totalDebits = totalDebits;
        this.totalCredits = totalCredits;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public Account getAccount() { return account; }
    public Customer getCustomer() { return customer; }
    public List<Transaction> getTransactions() { return transactions; }
    public BigDecimal getTotalDebits() { return totalDebits; }
    public BigDecimal getTotalCredits() { return totalCredits; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
}
