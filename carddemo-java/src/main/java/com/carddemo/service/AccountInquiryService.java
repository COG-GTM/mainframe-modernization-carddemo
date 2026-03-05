package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class AccountInquiryService {
    private final AccountRepository accountRepository;

    public AccountInquiryService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Map<String, Object> inquire(Long acctId) {
        Account account = accountRepository.findById(acctId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + acctId));
        return Map.of(
            "acctId", account.getAcctId(),
            "status", account.getActiveStatus() != null ? account.getActiveStatus() : "",
            "balance", account.getCurrBal() != null ? account.getCurrBal() : BigDecimal.ZERO,
            "creditLimit", account.getCreditLimit() != null ? account.getCreditLimit() : BigDecimal.ZERO
        );
    }
}
