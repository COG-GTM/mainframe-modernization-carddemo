package com.carddemo.repository.memory;

import com.carddemo.domain.Account;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** ACCTDATA backed by a map seeded from acctdata.txt. */
@Repository
public class InMemoryAccountRepository implements AccountRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public InMemoryAccountRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        accounts.clear();
        dataSet.accounts().forEach(account -> accounts.put(account.getAccountId(), Account.parse(account.format())));
    }

    @Override
    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(accounts.get(RecordKeys.accountId(accountId)));
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public Account save(Account account) {
        accounts.put(RecordKeys.accountId(account.getAccountId()), account);
        return account;
    }

    @Override
    public boolean existsById(String accountId) {
        return accounts.containsKey(RecordKeys.accountId(accountId));
    }
}
