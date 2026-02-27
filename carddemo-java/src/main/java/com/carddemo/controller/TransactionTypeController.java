package com.carddemo.controller;

import com.carddemo.entity.TransactionType;
import com.carddemo.service.TransactionTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction-types")
public class TransactionTypeController {
    private final TransactionTypeService transactionTypeService;

    public TransactionTypeController(TransactionTypeService transactionTypeService) {
        this.transactionTypeService = transactionTypeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionType>> listTransactionTypes(Pageable pageable) {
        return ResponseEntity.ok(transactionTypeService.listTransactionTypes(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionType> createTransactionType(@RequestBody TransactionType type) {
        return ResponseEntity.ok(transactionTypeService.createTransactionType(type));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionType> updateTransactionType(@PathVariable String code, @RequestBody TransactionType type) {
        return ResponseEntity.ok(transactionTypeService.updateTransactionType(code, type));
    }
}
