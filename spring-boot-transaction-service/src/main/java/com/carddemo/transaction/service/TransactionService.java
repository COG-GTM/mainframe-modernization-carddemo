package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.AddTransactionRequest;
import com.carddemo.transaction.dto.AddTransactionResponse;
import com.carddemo.transaction.dto.TransactionResponse;

/**
 * Service interface for transaction operations.
 *
 * Encapsulates the business logic from COTRN02C's PROCEDURE DIVISION,
 * including validation, cross-reference lookups, and transaction creation.
 */
public interface TransactionService {

    /**
     * Add a new transaction.
     *
     * Replaces the following COBOL paragraphs:
     *   - VALIDATE-INPUT-KEY-FIELDS (cross-reference lookup)
     *   - ADD-TRANSACTION (ID generation + record write)
     *   - WRITE-TRANSACT-FILE (VSAM write)
     *
     * @param request the transaction details from the client
     * @return response containing the new transaction ID and success message
     */
    AddTransactionResponse addTransaction(AddTransactionRequest request);

    /**
     * Get a transaction by its ID.
     *
     * Replaces: EXEC CICS READ DATASET(WS-TRANSACT-FILE)
     *
     * @param transactionId the transaction ID
     * @return the full transaction details
     */
    TransactionResponse getTransaction(Long transactionId);

    /**
     * Get the most recent (latest) transaction.
     *
     * Replaces the COBOL PF5 "Copy Last" flow:
     *   - STARTBR with HIGH-VALUES
     *   - READPREV
     *   - ENDBR
     *   - Copy fields to screen
     *
     * @return the most recent transaction details
     */
    TransactionResponse getLatestTransaction();
}
