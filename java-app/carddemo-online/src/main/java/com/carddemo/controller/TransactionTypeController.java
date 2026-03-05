package com.carddemo.controller;

import com.carddemo.entity.TransactionType;
import com.carddemo.service.TransactionTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction type management controller — replaces COTRTLIC.cbl and COTRTUPC.cbl.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/transaction-types")
@PreAuthorize("hasRole('ADMIN')")
public class TransactionTypeController {

    private final TransactionTypeService transactionTypeService;

    public TransactionTypeController(TransactionTypeService transactionTypeService) {
        this.transactionTypeService = transactionTypeService;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionType>> listTypes(Pageable pageable) {
        return ResponseEntity.ok(transactionTypeService.listTransactionTypes(pageable));
    }

    @PostMapping
    public ResponseEntity<TransactionType> addType(@RequestBody TransactionType type) {
        return ResponseEntity.ok(transactionTypeService.addTransactionType(type));
    }

    @PutMapping("/{typeCode}")
    public ResponseEntity<TransactionType> updateType(@PathVariable String typeCode,
                                                       @RequestBody TransactionType type) {
        return ResponseEntity.ok(transactionTypeService.updateTransactionType(typeCode, type));
    }

    @DeleteMapping("/{typeCode}")
    public ResponseEntity<Void> deleteType(@PathVariable String typeCode) {
        transactionTypeService.deleteTransactionType(typeCode);
        return ResponseEntity.noContent().build();
    }
}
