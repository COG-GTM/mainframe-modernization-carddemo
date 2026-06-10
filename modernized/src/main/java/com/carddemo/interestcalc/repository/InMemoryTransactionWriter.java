package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.TransactionRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory stand-in for the sequential TRANSACT-FILE output dataset. */
public class InMemoryTransactionWriter implements TransactionWriter {

    private final List<TransactionRecord> transactions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void write(TransactionRecord transaction) {
        transactions.add(transaction);
    }

    public List<TransactionRecord> getTransactions() {
        return List.copyOf(transactions);
    }
}
