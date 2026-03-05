package com.carddemo.controller;

import com.carddemo.dto.TransactionCreateRequest;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.Transaction;
import com.carddemo.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction management controller — replaces COTRN00C, COTRN01C, COTRN02C CICS transactions.
 */
@RestController
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/api/accounts/{acctId}/transactions")
    public ResponseEntity<Page<Transaction>> listTransactions(@PathVariable Long acctId, Pageable pageable) {
        return ResponseEntity.ok(transactionService.listTransactions(acctId, pageable));
    }

    @GetMapping("/api/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @PostMapping("/api/transactions")
    public ResponseEntity<DailyTransaction> addTransaction(@Valid @RequestBody TransactionCreateRequest dto) {
        return ResponseEntity.ok(transactionService.addTransaction(dto));
    }
}
