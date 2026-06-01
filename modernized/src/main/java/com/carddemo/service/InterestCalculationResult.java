package com.carddemo.service;

import java.math.BigDecimal;

/**
 * Summary of one interest-calculation run (counters CBACT04C keeps in WORKING-STORAGE plus the
 * total interest written). Returned to the caller / batch step for logging and assertions.
 *
 * @param recordsRead      category-balance records read (WS-RECORD-COUNT)
 * @param transactionsWritten interest transactions written (WS-TRANID-SUFFIX)
 * @param accountsUpdated  account records rewritten
 * @param totalInterest    sum of all interest written across accounts
 */
public record InterestCalculationResult(
        long recordsRead,
        long transactionsWritten,
        long accountsUpdated,
        BigDecimal totalInterest) {
}
