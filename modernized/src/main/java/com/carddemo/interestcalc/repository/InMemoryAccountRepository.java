package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.AccountRecord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory stand-in for the ACCOUNT-FILE VSAM KSDS. */
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<Long, AccountRecord> accounts = new ConcurrentHashMap<>();

    public void load(AccountRecord account) {
        accounts.put(account.getAccountId(), account);
    }

    @Override
    public Optional<AccountRecord> findById(long accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public void update(AccountRecord account) {
        accounts.put(account.getAccountId(), account);
    }
}
