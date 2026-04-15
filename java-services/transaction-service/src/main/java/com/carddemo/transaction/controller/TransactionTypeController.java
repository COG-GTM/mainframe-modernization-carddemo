package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.TransactionTypeDto;
import com.carddemo.transaction.service.TransactionTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * REST controller for transaction type operations.
 * Migrated from COBOL:
 * - GET /api/transaction-types         -> COTRTLIC (Transaction Type List)
 * - PUT /api/transaction-types/{code}  -> COTRTUPC (Transaction Type Update)
 */
@RestController
@RequestMapping("/api/transaction-types")
public class TransactionTypeController {

    private final TransactionTypeService transactionTypeService;

    public TransactionTypeController(TransactionTypeService transactionTypeService) {
        this.transactionTypeService = transactionTypeService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionTypeDto>> listTransactionTypes() {
        List<TransactionTypeDto> types = transactionTypeService.listTransactionTypes();
        return ResponseEntity.ok(types);
    }

    @PutMapping("/{typeCode}")
    public ResponseEntity<TransactionTypeDto> updateTransactionType(
            @PathVariable String typeCode,
            @RequestBody TransactionTypeDto dto) {
        TransactionTypeDto updated = transactionTypeService.updateTransactionType(typeCode, dto);
        return ResponseEntity.ok(updated);
    }
}
