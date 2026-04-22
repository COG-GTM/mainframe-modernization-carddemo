package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.TransactionCreateRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for transaction operations.
 * Migrated from COBOL:
 * - GET  /api/transactions            -> COTRN00C (Transaction List)
 * - GET  /api/transactions/{id}       -> COTRN01C (Transaction View)
 * - POST /api/transactions            -> COTRN02C (Transaction Add)
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> listTransactions(
            @RequestParam(required = false) String cardNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        TransactionListResponse response = transactionService.listTransactions(cardNumber, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String transactionId) {
        TransactionDto dto = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionCreateRequest request) {
        TransactionDto dto = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
