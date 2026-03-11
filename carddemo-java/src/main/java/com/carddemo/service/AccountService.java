package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account updateAccount(Long id, Account updated) {
        Account existing = getAccount(id);
        if (updated.getActiveStatus() != null) existing.setActiveStatus(updated.getActiveStatus());
        if (updated.getCreditLimit() != null) existing.setCreditLimit(updated.getCreditLimit());
        if (updated.getCashCreditLimit() != null) existing.setCashCreditLimit(updated.getCashCreditLimit());
        if (updated.getExpirationDate() != null) existing.setExpirationDate(updated.getExpirationDate());
        if (updated.getReissueDate() != null) existing.setReissueDate(updated.getReissueDate());
        if (updated.getAddrZip() != null) existing.setAddrZip(updated.getAddrZip());
        if (updated.getGroupId() != null) existing.setGroupId(updated.getGroupId());
        return accountRepository.save(existing);
    }
}
