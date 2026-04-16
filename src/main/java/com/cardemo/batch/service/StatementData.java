package com.cardemo.batch.service;

import com.cardemo.batch.entity.Account;
import com.cardemo.batch.entity.Customer;
import com.cardemo.batch.entity.TransactionRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * Holds all data needed to generate a single statement.
 * Corresponds to the data gathered per XREF iteration in CBSTM03A
 * (sections 1000-MAINLINE through 5000-CREATE-STATEMENT).
 */
public class StatementData {

    private final String cardNum;
    private final Customer customer;
    private final Account account;
    private final List<TransactionRecord> transactions;
    private final BigDecimal totalAmount;

    public StatementData(String cardNum,
                         Customer customer,
                         Account account,
                         List<TransactionRecord> transactions,
                         BigDecimal totalAmount) {
        this.cardNum = cardNum;
        this.customer = customer;
        this.account = account;
        this.transactions = transactions;
        this.totalAmount = totalAmount;
    }

    public String getCardNum() {
        return cardNum;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Account getAccount() {
        return account;
    }

    public List<TransactionRecord> getTransactions() {
        return transactions;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
