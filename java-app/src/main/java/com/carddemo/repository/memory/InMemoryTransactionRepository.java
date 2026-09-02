package com.carddemo.repository.memory;

import com.carddemo.domain.Transaction;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TRANSACT backed by a map.
 *
 * <p>Seeded from dailytran.txt: the sample repository has no posted transaction file, and the daily
 * transactions are the records the posting job (CBTRN02C) turns into TRANSACT records.
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, Transaction> transactions = new LinkedHashMap<>();

    public InMemoryTransactionRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        transactions.clear();
        dataSet.dailyTransactions().forEach(transaction -> transactions.put(transaction.getTransactionId(),
                Transaction.parse(transaction.format())));
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return Optional.ofNullable(transactions.get(RecordKeys.transactionId(transactionId)));
    }

    @Override
    public List<Transaction> findByCardNumber(String cardNumber) {
        String key = RecordKeys.cardNumber(cardNumber);
        return transactions.values().stream().filter(transaction -> key.equals(transaction.getCardNumber())).toList();
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }

    @Override
    public Transaction save(Transaction transaction) {
        transactions.put(RecordKeys.transactionId(transaction.getTransactionId()), transaction);
        return transaction;
    }
}
