package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.*;
import com.carddemo.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for transaction operations.
 * Implements the four COBOL programs as REST endpoints:
 *   COTRN00C -> GET  /api/v1/transactions        (list with pagination)
 *   COTRN01C -> GET  /api/v1/transactions/{id}    (view)
 *   COTRN02C -> POST /api/v1/transactions          (add)
 *   COBIL00C -> POST /api/v1/transactions/bill-payment (bill payment)
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "CardDemo Transaction Processing API")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "List transactions",
            description = "Paginated list of transactions. Port of COTRN00C.")
    public ResponseEntity<TransactionListResponse> listTransactions(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, matching COBOL 10 per page)")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Filter by transaction ID (must be numeric)")
            @RequestParam(required = false) String tranId) {
        return ResponseEntity.ok(transactionService.listTransactions(page, size, tranId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "View transaction",
            description = "View a single transaction by ID. Port of COTRN01C.")
    public ResponseEntity<TransactionResponse> viewTransaction(
            @PathVariable String id) {
        return ResponseEntity.ok(transactionService.viewTransaction(id));
    }

    @PostMapping
    @Operation(summary = "Add transaction",
            description = "Add a new transaction with full validation. Port of COTRN02C.")
    public ResponseEntity<TransactionResponse> addTransaction(
            @RequestBody TransactionAddRequest request) {
        TransactionResponse response = transactionService.addTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bill-payment")
    @Operation(summary = "Process bill payment",
            description = "Atomic bill payment: create transaction + update account balance. Port of COBIL00C.")
    public ResponseEntity<BillPaymentResponse> processBillPayment(
            @RequestBody BillPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.processBillPayment(request));
    }
}
