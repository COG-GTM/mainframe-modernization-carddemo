package com.carddemo.batch.interest;

import java.math.BigDecimal;

/**
 * COBOL program: CBACT04C — run counters of the interest calculation.
 *
 * @param recordsRead          WS-RECORD-COUNT, TCATBALF records read
 * @param transactionsWritten  WS-TRANID-SUFFIX, interest transactions written to TRANSACT
 * @param accountsUpdated      number of 1050-UPDATE-ACCOUNT rewrites of the ACCTFILE record
 * @param totalInterestPosted  sum of the WS-TOTAL-INT amounts actually added to accounts
 */
public record InterestCalculationResult(long recordsRead,
                                        long transactionsWritten,
                                        long accountsUpdated,
                                        BigDecimal totalInterestPosted) {
}
