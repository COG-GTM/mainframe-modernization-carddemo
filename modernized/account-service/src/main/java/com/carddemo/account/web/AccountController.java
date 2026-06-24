package com.carddemo.account.web;

import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the account data as a REST resource — the modern equivalent of the
 * {@code ACCTDAT} VSAM file. {@code GET /api/accounts} reproduces CBACT01C's
 * sequential read (ordered by key); {@code GET /api/accounts/{id}} is a keyed
 * VSAM READ.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public List<AccountResponse> listAccounts() {
        return accountRepository.findAllByOrderByAcctIdAsc().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{acctId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String acctId) {
        return accountRepository.findById(acctId)
                .map(AccountResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
