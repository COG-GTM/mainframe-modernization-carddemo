package com.carddemo.account.controller;

import com.carddemo.account.dto.AccountListResponse;
import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for account operations.
 * Migrated from: COACTVWC.cbl (Account View — CAVW) and COACTUPC.cbl (Account Update — CAUP)
 * CICS operations: READ ACCTDAT, READ CUSTDAT, STARTBR/READNEXT CXACAIX, REWRITE ACCTDAT, REWRITE CUSTDAT
 * VSAM files: ACCTDAT (KSDS), CUSTDAT (KSDS), CXACAIX (AIX PATH)
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "Account management — migrated from COACTVWC/COACTUPC COBOL programs")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * View account with customer and card details.
     * Replaces COACTVWC.cbl (Transaction CAVW).
     */
    @GetMapping("/{accountId}")
    @Operation(
            summary = "View account details",
            description = "Returns combined account, customer, and linked card data. Replaces COACTVWC.cbl (CAVW).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account found"),
                    @ApiResponse(responseCode = "404", description = "Account not found")
            }
    )
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "Account ID (11-digit numeric)") @PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }

    /**
     * Update account and customer atomically.
     * Replaces COACTUPC.cbl (Transaction CAUP).
     */
    @PutMapping("/{accountId}")
    @Operation(
            summary = "Update account and customer",
            description = "Atomically updates both account and customer records. Replaces COACTUPC.cbl (CAUP). "
                    + "Both updates happen in a single @Transactional operation, matching the original CICS unit-of-work.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account updated"),
                    @ApiResponse(responseCode = "404", description = "Account not found"),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    public ResponseEntity<AccountResponse> updateAccount(
            @Parameter(description = "Account ID (11-digit numeric)") @PathVariable Long accountId,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, request));
    }

    /**
     * List accounts with pagination.
     */
    @GetMapping
    @Operation(
            summary = "List accounts",
            description = "Returns a paginated list of accounts.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Accounts retrieved")
            }
    )
    public ResponseEntity<AccountListResponse> listAccounts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(accountService.listAccounts(page, size));
    }
}
