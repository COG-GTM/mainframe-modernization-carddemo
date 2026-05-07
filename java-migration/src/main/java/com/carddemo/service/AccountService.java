package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Account update(Account account) {
        return accountRepository.save(account);
    }

    public void delete(Long id) {
        accountRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Account> findByActiveStatus(String activeStatus) {
        return accountRepository.findByActiveStatus(activeStatus);
    }

    @Transactional(readOnly = true)
    public List<Account> findByGroupId(String groupId) {
        return accountRepository.findByGroupId(groupId);
    }
}
