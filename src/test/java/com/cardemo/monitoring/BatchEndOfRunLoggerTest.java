package com.cardemo.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BatchEndOfRunLoggerTest {

    private TransactionMetrics transactionMetrics;
    private BatchEndOfRunLogger logger;

    @BeforeEach
    void setUp() {
        var registry = new SimpleMeterRegistry();
        transactionMetrics = new TransactionMetrics(registry);
        logger = new BatchEndOfRunLogger(transactionMetrics);
    }

    @Test
    void formatCobolCounterZero() {
        assertEquals("000000000", BatchEndOfRunLogger.formatCobolCounter(0));
    }

    @Test
    void formatCobolCounterSmallNumber() {
        assertEquals("000001234", BatchEndOfRunLogger.formatCobolCounter(1234));
    }

    @Test
    void formatCobolCounterLargeNumber() {
        assertEquals("999999999", BatchEndOfRunLogger.formatCobolCounter(999999999));
    }

    @Test
    void formatCobolCounterTwelve() {
        assertEquals("000000012", BatchEndOfRunLogger.formatCobolCounter(12));
    }

    @Test
    void logTransactionPostingSummaryDoesNotThrow() {
        transactionMetrics.incrementTransactionsProcessed(1234);
        transactionMetrics.incrementTransactionsRejected(12);

        assertDoesNotThrow(() -> logger.logTransactionPostingSummary());
    }

    @Test
    void logInterestCalculationSummaryDoesNotThrow() {
        transactionMetrics.incrementInterestRecordsProcessed(5678);

        assertDoesNotThrow(() -> logger.logInterestCalculationSummary());
    }

    @Test
    void logBatchSummaryDoesNotThrow() {
        assertDoesNotThrow(() -> logger.logBatchSummary("TESTPROG", 100, 5));
    }
}
