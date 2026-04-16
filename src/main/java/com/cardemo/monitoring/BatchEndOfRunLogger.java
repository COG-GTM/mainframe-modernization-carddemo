package com.cardemo.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * End-of-run structured logging that preserves COBOL DISPLAY statement format.
 *
 * Original COBOL output (CBTRN02C):
 *   DISPLAY 'TRANSACTIONS PROCESSED :' WS-TRANSACTION-COUNT
 *   DISPLAY 'TRANSACTIONS REJECTED  :' WS-REJECT-COUNT
 *
 * Original COBOL output (CBACT04C):
 *   DISPLAY 'RECORDS PROCESSED: 000001234'
 *   DISPLAY 'RECORDS REJECTED:  000000012'
 */
@Component
public class BatchEndOfRunLogger {

    private static final Logger log = LoggerFactory.getLogger(BatchEndOfRunLogger.class);

    private final TransactionMetrics transactionMetrics;

    public BatchEndOfRunLogger(TransactionMetrics transactionMetrics) {
        this.transactionMetrics = transactionMetrics;
    }

    /**
     * Log end-of-run summary for transaction posting (CBTRN02C equivalent).
     * Preserves the exact COBOL DISPLAY text format with 9-digit zero-padded counts.
     */
    public void logTransactionPostingSummary() {
        long processed = (long) transactionMetrics.getTransactionsProcessedCount();
        long rejected = (long) transactionMetrics.getTransactionsRejectedCount();

        String processedFormatted = formatCobolCounter(processed);
        String rejectedFormatted = formatCobolCounter(rejected);

        log.info("START OF EXECUTION OF PROGRAM CBTRN02C");
        log.info("RECORDS PROCESSED: {}", processedFormatted);
        log.info("RECORDS REJECTED:  {}", rejectedFormatted);
        log.info("TRANSACTIONS PROCESSED :{}", processedFormatted);
        log.info("TRANSACTIONS REJECTED  :{}", rejectedFormatted);
        log.info("END OF EXECUTION OF PROGRAM CBTRN02C");
    }

    /**
     * Log end-of-run summary for interest calculation (CBACT04C equivalent).
     */
    public void logInterestCalculationSummary() {
        long recordCount = (long) transactionMetrics.getInterestRecordsProcessedCount();
        String recordsFormatted = formatCobolCounter(recordCount);

        log.info("START OF EXECUTION OF PROGRAM CBACT04C");
        log.info("RECORDS PROCESSED: {}", recordsFormatted);
        log.info("END OF EXECUTION OF PROGRAM CBACT04C");
    }

    /**
     * Log end-of-run summary with explicit counts (for batch jobs that pass counts directly).
     */
    public void logBatchSummary(String programName, long recordsProcessed, long recordsRejected) {
        String processedFormatted = formatCobolCounter(recordsProcessed);
        String rejectedFormatted = formatCobolCounter(recordsRejected);

        log.info("START OF EXECUTION OF PROGRAM {}", programName);
        log.info("RECORDS PROCESSED: {}", processedFormatted);
        log.info("RECORDS REJECTED:  {}", rejectedFormatted);
        log.info("END OF EXECUTION OF PROGRAM {}", programName);
    }

    /**
     * Format a count in COBOL PIC 9(09) style: 9-digit zero-padded.
     */
    public static String formatCobolCounter(long count) {
        return String.format("%09d", count);
    }
}
