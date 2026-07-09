package com.carddemo.web.transaction.dto;

/**
 * Successful add-transaction result — the generated {@code TRAN-ID} and the verbatim
 * {@code COTRN02C} success message ("Transaction added successfully.  Your Tran ID is ...").
 *
 * @param tranId  the newly generated 16-char TRAN-ID (last id + 1, zero-padded)
 * @param message the COBOL confirmation message
 */
public record TransactionAddResponse(String tranId, String message) {
}
