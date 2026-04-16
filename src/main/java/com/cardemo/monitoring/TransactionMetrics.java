package com.cardemo.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Metrics ported from COBOL WORKING-STORAGE counters in CBTRN02C and CBACT04C.
 *
 * CBTRN02C counters:
 *   WS-TRANSACTION-COUNT PIC 9(09) -> transactions_processed_total
 *   WS-REJECT-COUNT      PIC 9(09) -> transactions_rejected_total
 *
 * CBACT04C counter:
 *   WS-RECORD-COUNT      PIC 9(09) -> interest_records_processed_total
 */
@Component
public class TransactionMetrics {

    private final Counter transactionsProcessed;
    private final Counter transactionsRejected;
    private final Counter interestRecordsProcessed;

    public TransactionMetrics(MeterRegistry registry) {
        this.transactionsProcessed = Counter.builder("transactions_processed_total")
                .description("Total transactions processed (CBTRN02C WS-TRANSACTION-COUNT)")
                .tag("source", "CBTRN02C")
                .register(registry);

        this.transactionsRejected = Counter.builder("transactions_rejected_total")
                .description("Total transactions rejected (CBTRN02C WS-REJECT-COUNT)")
                .tag("source", "CBTRN02C")
                .register(registry);

        this.interestRecordsProcessed = Counter.builder("interest_records_processed_total")
                .description("Total interest records processed (CBACT04C WS-RECORD-COUNT)")
                .tag("source", "CBACT04C")
                .register(registry);
    }

    public void incrementTransactionsProcessed() {
        transactionsProcessed.increment();
    }

    public void incrementTransactionsProcessed(double amount) {
        transactionsProcessed.increment(amount);
    }

    public void incrementTransactionsRejected() {
        transactionsRejected.increment();
    }

    public void incrementTransactionsRejected(double amount) {
        transactionsRejected.increment(amount);
    }

    public void incrementInterestRecordsProcessed() {
        interestRecordsProcessed.increment();
    }

    public void incrementInterestRecordsProcessed(double amount) {
        interestRecordsProcessed.increment(amount);
    }

    public double getTransactionsProcessedCount() {
        return transactionsProcessed.count();
    }

    public double getTransactionsRejectedCount() {
        return transactionsRejected.count();
    }

    public double getInterestRecordsProcessedCount() {
        return interestRecordsProcessed.count();
    }
}
