package com.carddemo.repository;

import com.carddemo.domain.Account;

import java.util.List;
import java.util.Optional;

/** ACCTDATA (VSAM KSDS keyed on ACCT-ID). */
public interface AccountRepository {

    Optional<Account> findById(String accountId);

    List<Account> findAll();

    Account save(Account account);

    boolean existsById(String accountId);
}
