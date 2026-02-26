package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.AddTransactionRequest;
import com.carddemo.transaction.dto.AddTransactionResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for transaction operations.
 *
 * Replaces the CICS BMS screen interaction model of COTRN02C:
 *   - POST /api/v1/transactions       -> ENTER key + Y confirm
 *   - GET  /api/v1/transactions/{id}  -> Direct record lookup
 *   - GET  /api/v1/transactions/latest -> PF5 (Copy Last Transaction)
 *
 * The CICS pseudo-conversational flow (SEND MAP / RECEIVE MAP / RETURN TRANSID)
 * is replaced by stateless REST request/response cycles.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Add a new transaction.
     *
     * Replaces: PROCESS-ENTER-KEY -> VALIDATE-INPUT-KEY-FIELDS
     *           -> VALIDATE-INPUT-DATA-FIELDS -> ADD-TRANSACTION
     *           -> WRITE-TRANSACT-FILE
     *
     * The confirmation step (CONFIRMI = 'Y') is implicit in the POST request;
     * submitting the request is the confirmation.
     *
     * @param request the validated transaction input
     * @return 201 Created with the new transaction ID
     */
    @PostMapping
    public ResponseEntity<AddTransactionResponse> addTransaction(
            @Valid @RequestBody AddTransactionRequest request) {
        AddTransactionResponse response = transactionService.addTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a transaction by ID.
     *
     * @param id the transaction ID
     * @return the transaction details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
        TransactionResponse response = transactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the most recently added transaction.
     *
     * Replaces the PF5 (Copy Last Transaction) flow in COTRN02C:
     *   COPY-LAST-TRAN-DATA paragraph which browses to the end of the
     *   TRANSACT file and reads the last record.
     *
     * @return the most recent transaction details
     */
    @GetMapping("/latest")
    public ResponseEntity<TransactionResponse> getLatestTransaction() {
        TransactionResponse response = transactionService.getLatestTransaction();
        return ResponseEntity.ok(response);
    }
}
