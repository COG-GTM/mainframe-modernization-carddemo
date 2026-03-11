package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Account operations.
 * <p>
 * Methods mirror the COBOL CICS / batch operations:
 * <ul>
 *   <li>{@link #findById} – CICS READ by key (as in {@code COACTVWC.cbl})</li>
 *   <li>{@link #updateAccount} – REWRITE (as in {@code CBTRN02C.cbl} line 554)</li>
 *   <li>{@link #findAll} – sequential READ (as in {@code CBACT01C.cbl})</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Retrieve an account by its primary key.
     * Mirrors CICS READ by key in {@code COACTVWC.cbl}.
     *
     * @param acctId the 11-digit account identifier
     * @return an {@link Optional} containing the account if found
     */
    public Optional<Account> findById(Long acctId) {
        return accountRepository.findById(acctId);
    }

    /**
     * Persist changes to an existing account record.
     * Mirrors REWRITE in {@code CBTRN02C.cbl} (line 554).
     *
     * @param account the account entity with updated fields
     * @return the saved account
     */
    @Transactional
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Retrieve all account records (sequential read).
     * Mirrors the sequential file read loop in {@code CBACT01C.cbl}.
     *
     * @return list of all accounts
     */
    public List<Account> findAll() {
        return accountRepository.findAll();
    }
}
