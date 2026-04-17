package com.carddemo.transaction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for batch posting daily transactions.
 * Translates CBTRN02C batch input (DALYTRAN-RECORD from CVTRA06Y.cpy).
 */
public class BatchPostRequest {

    @NotEmpty(message = "Transaction list cannot be empty")
    @Valid
    private List<TransactionRequest> transactions;

    public List<TransactionRequest> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionRequest> transactions) {
        this.transactions = transactions;
    }
}
