package com.carddemo.controller;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.entity.Account;
import com.carddemo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Account management controller — replaces COACTVWC, COACTUPC CICS transactions.
 */
@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{acctId}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable Long acctId) {
        return ResponseEntity.ok(accountService.getAccount(acctId));
    }

    @PutMapping("/{acctId}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long acctId,
                                                  @Valid @RequestBody AccountUpdateRequest dto) {
        return ResponseEntity.ok(accountService.updateAccount(acctId, dto));
    }
}
