package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.BatchPostRequest;
import com.carddemo.transaction.dto.TransactionRequest;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller for transaction operations.
 * Translates CICS transaction programs:
 * - CT00 (COTRN00C): GET /transactions — list with filters
 * - CT01 (COTRN01C): GET /transactions/{id} — view by ID
 * - CT02 (COTRN02C): POST /transactions — add new transaction
 * - CBTRN02C batch:  POST /transactions/batch-post — batch validate & post
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * List transactions with optional filters.
     * Translates COTRN00C transaction list with STARTBR/READNEXT pagination.
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> listTransactions(
            @RequestParam(required = false) String cardNum,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TransactionResponse> transactions = transactionService.listTransactions(
                cardNum, startDate, endDate, page, size);
        return ResponseEntity.ok(transactions);
    }

    /**
     * View transaction by ID.
     * Translates COTRN01C READ TRANSACT-FILE by TRAN-ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add a new transaction.
     * Translates COTRN02C: validate card, save transaction, publish event.
     *
     * @param skipEvent when true, suppresses the transaction.posted RabbitMQ event.
     *                  Callers that manage account balance updates themselves (e.g.
     *                  BillPaymentSaga) should set this to true to avoid double-updating
     *                  the account balance.
     */
    @PostMapping
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestParam(defaultValue = "false") boolean skipEvent) {
        try {
            TransactionResponse response = transactionService.createTransaction(request, !skipEvent);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Batch post daily transactions.
     * Translates CBTRN02C: validate each transaction, post valid ones,
     * reject invalid ones, publish events for each posted transaction.
     */
    @PostMapping("/batch-post")
    public ResponseEntity<Map<String, Object>> batchPost(
            @Valid @RequestBody BatchPostRequest request) {
        Map<String, Object> result = transactionService.batchPostTransactions(
                request.getTransactions());
        return ResponseEntity.ok(result);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "transaction-service");
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
