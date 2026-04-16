package com.cardemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionMetricsTest {

    private MeterRegistry registry;
    private TransactionMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new TransactionMetrics(registry);
    }

    @Test
    void transactionsProcessedCounterIncrements() {
        metrics.incrementTransactionsProcessed();
        metrics.incrementTransactionsProcessed();
        metrics.incrementTransactionsProcessed();

        assertEquals(3.0, metrics.getTransactionsProcessedCount());
    }

    @Test
    void transactionsProcessedCounterIncrementsByAmount() {
        metrics.incrementTransactionsProcessed(100);

        assertEquals(100.0, metrics.getTransactionsProcessedCount());
    }

    @Test
    void transactionsRejectedCounterIncrements() {
        metrics.incrementTransactionsRejected();

        assertEquals(1.0, metrics.getTransactionsRejectedCount());
    }

    @Test
    void transactionsRejectedCounterIncrementsByAmount() {
        metrics.incrementTransactionsRejected(50);

        assertEquals(50.0, metrics.getTransactionsRejectedCount());
    }

    @Test
    void interestRecordsProcessedCounterIncrements() {
        metrics.incrementInterestRecordsProcessed();
        metrics.incrementInterestRecordsProcessed();

        assertEquals(2.0, metrics.getInterestRecordsProcessedCount());
    }

    @Test
    void interestRecordsProcessedCounterIncrementsByAmount() {
        metrics.incrementInterestRecordsProcessed(1234);

        assertEquals(1234.0, metrics.getInterestRecordsProcessedCount());
    }

    @Test
    void countersStartAtZero() {
        assertEquals(0.0, metrics.getTransactionsProcessedCount());
        assertEquals(0.0, metrics.getTransactionsRejectedCount());
        assertEquals(0.0, metrics.getInterestRecordsProcessedCount());
    }

    @Test
    void metricsAreRegisteredInRegistry() {
        var processedCounter = registry.find("transactions_processed_total").counter();
        var rejectedCounter = registry.find("transactions_rejected_total").counter();
        var interestCounter = registry.find("interest_records_processed_total").counter();

        assertNotNull(processedCounter);
        assertNotNull(rejectedCounter);
        assertNotNull(interestCounter);

        assertEquals("CBTRN02C", processedCounter.getId().getTag("source"));
        assertEquals("CBTRN02C", rejectedCounter.getId().getTag("source"));
        assertEquals("CBACT04C", interestCounter.getId().getTag("source"));
    }
}
