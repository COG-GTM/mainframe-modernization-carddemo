package com.carddemo.account.controller;

import com.carddemo.account.dto.AccountRequest;
import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.dto.BalanceUpdateRequest;
import com.carddemo.account.service.AccountService;
import com.carddemo.account.service.InterestCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for Account operations.
 *
 * Translates CICS transaction endpoints:
 *   CAVW (COACTVWC) -> GET  /accounts/{id}
 *   CAUP (COACTUPC) -> PUT  /accounts/{id}
 *   CBACT04C         -> POST /accounts/{id}/calculate-interest
 *   Health check     -> GET  /accounts/health
 *   Balance update   -> PUT  /accounts/{id}/balance
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final InterestCalculationService interestCalculationService;

    public AccountController(AccountService accountService,
                             InterestCalculationService interestCalculationService) {
        this.accountService = accountService;
        this.interestCalculationService = interestCalculationService;
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "account-service"
        ));
    }

    /**
     * View account details by account ID (11-digit numeric).
     * Translates COACTVWC (CICS transaction CAVW).
     *
     * Original COBOL flow:
     *   9200-GETCARDXREF-BYACCT -> 9300-GETACCTDATA-BYACCT -> 9400-GETCUSTDATA-BYCUST
     *   Then display fields on BMS map CACTVWA.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable("id") String id) {
        log.info("GET /accounts/{}", id);
        return accountService.getAccount(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update account (active status, credit limit, cash credit limit, group ID, zip).
     * Translates COACTUPC (CICS transaction CAUP).
     *
     * Original COBOL validates:
     *   - ACCT-ACTIVE-STATUS must be Y or N
     *   - ACCT-CREDIT-LIMIT must be valid signed numeric
     *   - ACCT-CASH-CREDIT-LIMIT must be valid signed numeric
     *   Then REWRITE ACCTDAT record.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable("id") String id,
            @RequestBody AccountRequest request) {
        log.info("PUT /accounts/{}", id);
        try {
            AccountResponse response = accountService.updateAccount(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Update failed for account {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Trigger interest calculation for an account.
     * Translates CBACT04C batch interest calculator.
     *
     * Original COBOL flow:
     *   1. Read TCATBALF sequentially for the account
     *   2. Look up interest rate from DISCGRP using ACCT-GROUP-ID + transaction category
     *   3. COMPUTE monthly_interest = (balance * rate) / 1200
     *   4. ADD monthly_interest TO ACCT-CURR-BAL
     *   5. Reset cycle credits/debits to zero
     *
     * @param id           account ID
     * @param interestRate optional annual interest rate (percentage). If not provided,
     *                     defaults to 22.99% (simulating DISCGRP lookup).
     */
    @PostMapping("/{id}/calculate-interest")
    public ResponseEntity<AccountResponse> calculateInterest(
            @PathVariable("id") String id,
            @RequestParam(value = "interestRate", required = false) BigDecimal interestRate) {
        log.info("POST /accounts/{}/calculate-interest, rate={}", id, interestRate);
        try {
            AccountResponse response = interestCalculationService.calculateInterest(id, interestRate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Interest calculation failed for account {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update account balance.
     * Exposed for other services (Transaction Service, Billing Service).
     *
     * Translates the balance update logic from CBACT04C paragraph 1050-UPDATE-ACCOUNT:
     *   ADD amount TO ACCT-CURR-BAL
     *   Update ACCT-CURR-CYC-CREDIT or ACCT-CURR-CYC-DEBIT based on sign
     */
    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> updateBalance(
            @PathVariable("id") String id,
            @RequestBody BalanceUpdateRequest request) {
        log.info("PUT /accounts/{}/balance, amount={}", id, request.getAmount());
        try {
            AccountResponse response = accountService.updateBalance(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Balance update failed for account {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
