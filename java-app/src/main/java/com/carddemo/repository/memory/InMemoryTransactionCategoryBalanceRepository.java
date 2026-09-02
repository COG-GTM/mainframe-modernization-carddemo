package com.carddemo.repository.memory;

import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.util.CobolCodec;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** TCATBALF category balances seeded from tcatbal.txt; mutated by the posting and interest jobs. */
@Repository
public class InMemoryTransactionCategoryBalanceRepository
        implements TransactionCategoryBalanceRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, TransactionCategoryBalance> balances = new LinkedHashMap<>();

    public InMemoryTransactionCategoryBalanceRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        balances.clear();
        dataSet.transactionCategoryBalances()
                .forEach(balance -> balances.put(balance.key(), TransactionCategoryBalance.parse(balance.format())));
    }

    @Override
    public Optional<TransactionCategoryBalance> find(String accountId, String typeCode, int categoryCode) {
        return Optional.ofNullable(balances.get(RecordKeys.accountId(accountId)
                + CobolCodec.encodeText(typeCode, 2) + CobolCodec.encodeNumeric(categoryCode, 4)));
    }

    @Override
    public List<TransactionCategoryBalance> findByAccountId(String accountId) {
        String key = RecordKeys.accountId(accountId);
        return balances.values().stream().filter(balance -> key.equals(balance.getAccountId())).toList();
    }

    @Override
    public List<TransactionCategoryBalance> findAll() {
        return new ArrayList<>(balances.values());
    }

    @Override
    public TransactionCategoryBalance save(TransactionCategoryBalance balance) {
        balances.put(balance.key(), balance);
        return balance;
    }
}
