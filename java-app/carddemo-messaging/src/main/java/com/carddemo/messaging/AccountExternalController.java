package com.carddemo.messaging;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST endpoints for external account queries — Option B (REST).
 * Translates COACCT01.cbl and CODATE01.cbl for modern REST integrations.
 */
@RestController
@RequestMapping("/api/external")
public class AccountExternalController {

    private final AccountRepository accountRepository;

    public AccountExternalController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Get account by ID — replaces COACCT01.cbl MQ query.
     */
    @GetMapping("/accounts/{acctId}")
    public ResponseEntity<Account> getAccount(@PathVariable Long acctId) {
        return accountRepository.findById(acctId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get system date — replaces CODATE01.cbl.
     */
    @GetMapping("/system-date")
    public ResponseEntity<Map<String, String>> getSystemDate() {
        return ResponseEntity.ok(Map.of(
            "date", LocalDate.now().toString(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
