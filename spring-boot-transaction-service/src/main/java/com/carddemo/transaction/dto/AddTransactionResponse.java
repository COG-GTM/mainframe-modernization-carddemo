package com.carddemo.transaction.dto;

/**
 * Response DTO for the add-transaction endpoint.
 * Replaces the success/error messages in COTRN2AO (ERRMSGO field).
 */
public class AddTransactionResponse {

    private Long transactionId;
    private String message;

    public AddTransactionResponse() {
    }

    public AddTransactionResponse(Long transactionId, String message) {
        this.transactionId = transactionId;
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
