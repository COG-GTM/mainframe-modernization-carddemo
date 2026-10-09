package com.carddemo.web;

import com.carddemo.service.AccountService;
import com.carddemo.web.dto.AccountUpdateRequest;
import com.carddemo.web.dto.AccountView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** COACTVWC (view) and COACTUPC (update). */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    public AccountView view(@PathVariable Long accountId) {
        return accountService.view(accountId);
    }

    @PutMapping("/{accountId}")
    public AccountView update(@PathVariable Long accountId, @RequestBody AccountUpdateRequest request) {
        return accountService.update(accountId, request);
    }
}
