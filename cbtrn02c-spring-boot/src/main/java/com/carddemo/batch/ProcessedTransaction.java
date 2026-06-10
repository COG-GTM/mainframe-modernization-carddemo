package com.carddemo.batch;

import com.carddemo.entity.DailyTransaction;
import com.carddemo.service.ValidationResult;

/**
 * Carrier passed from the {@link TransactionItemProcessor} to the writer: the original
 * daily transaction together with the outcome of validation/posting.
 */
public record ProcessedTransaction(DailyTransaction transaction, ValidationResult result) {
}
