package com.cardemo.service;

import com.cardemo.entity.Account;
import com.cardemo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Account entity operations.
 * Mirrors COBOL operations: keyed READ, sequential READ, REWRITE, WRITE.
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /** Mirrors COBOL keyed READ on ACCTDAT. */
    @Transactional(readOnly = true)
    public Optional<Account> findById(Long acctId) {
        return accountRepository.findById(acctId);
    }

    /** Mirrors COBOL sequential READ on ACCTDAT. */
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    /** Mirrors COBOL WRITE to ACCTDAT. */
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    /** Mirrors COBOL REWRITE on ACCTDAT. */
    public Account update(Account account) {
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> findByActiveStatus(String status) {
        return accountRepository.findByAcctActiveStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Account> findByGroupId(String groupId) {
        return accountRepository.findByAcctGroupId(groupId);
    }
}
