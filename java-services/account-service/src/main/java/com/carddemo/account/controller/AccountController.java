package com.carddemo.account.controller;

import com.carddemo.account.dto.AccountDto;
import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping
    public ResponseEntity<Page<AccountDto>> listAccounts(Pageable pageable) {
        return ResponseEntity.ok(accountService.listAccounts(pageable));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, request));
    }

    @GetMapping("/card/{cardNumber}")
    public ResponseEntity<AccountDto> getAccountByCardNumber(@PathVariable String cardNumber) {
        return ResponseEntity.ok(accountService.getAccountByCardNumber(cardNumber));
    }
}
