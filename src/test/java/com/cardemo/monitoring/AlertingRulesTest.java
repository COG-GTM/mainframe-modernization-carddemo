package com.cardemo.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertingRulesTest {

    private TransactionMetrics transactionMetrics;
    private BatchJobMetrics batchJobMetrics;
    private AlertingRules alertingRules;

    @BeforeEach
    void setUp() {
        var registry = new SimpleMeterRegistry();
        transactionMetrics = new TransactionMetrics(registry);
        batchJobMetrics = new BatchJobMetrics(registry);
        alertingRules = new AlertingRules(transactionMetrics, batchJobMetrics);
    }

    @Test
    void rejectionRateWithinThreshold() {
        transactionMetrics.incrementTransactionsProcessed(100);
        transactionMetrics.incrementTransactionsRejected(5);

        assertFalse(alertingRules.checkRejectionRate());
    }

    @Test
    void rejectionRateExceedsThreshold() {
        transactionMetrics.incrementTransactionsProcessed(80);
        transactionMetrics.incrementTransactionsRejected(20);

        assertTrue(alertingRules.checkRejectionRate());
    }

    @Test
    void rejectionRateWithExplicitCountsExceedsThreshold() {
        assertTrue(alertingRules.checkRejectionRate(80, 20));
    }

    @Test
    void rejectionRateWithExplicitCountsWithinThreshold() {
        assertFalse(alertingRules.checkRejectionRate(95, 5));
    }

    @Test
    void rejectionRateWithZeroCounts() {
        assertFalse(alertingRules.checkRejectionRate());
    }

    @Test
    void zeroTransactionsAlertWhenNoneProcessed() {
        assertTrue(alertingRules.checkZeroTransactions());
    }

    @Test
    void noZeroTransactionsAlertWhenProcessed() {
        transactionMetrics.incrementTransactionsProcessed(1);

        assertFalse(alertingRules.checkZeroTransactions());
    }

    @Test
    void zeroTransactionsWithExplicitCount() {
        assertTrue(alertingRules.checkZeroTransactions(0));
        assertFalse(alertingRules.checkZeroTransactions(1));
    }

    @Test
    void jobDurationAnomalyNotDetectedWithNoHistory() {
        assertFalse(alertingRules.checkJobDurationAnomaly(5000));
    }

    @Test
    void jobDurationAnomalyDetectedWhenExceeds2xAverage() {
        batchJobMetrics.recordJobDuration(1000);
        batchJobMetrics.recordJobDuration(1000);
        batchJobMetrics.recordJobDuration(1000);

        assertTrue(alertingRules.checkJobDurationAnomaly(3000));
    }

    @Test
    void jobDurationAnomalyNotDetectedWithinThreshold() {
        batchJobMetrics.recordJobDuration(1000);
        batchJobMetrics.recordJobDuration(1000);

        assertFalse(alertingRules.checkJobDurationAnomaly(1500));
    }

    @Test
    void evaluateAllReturnsSummary() {
        var summary = alertingRules.evaluateAll();
        assertTrue(summary.zeroTransactionsDetected());
        assertFalse(summary.rejectionRateExceeded());
    }

    @Test
    void alertSummaryHasAlertsWhenAnyAlertFires() {
        var summary = new AlertingRules.AlertSummary(false, false, true);
        assertTrue(summary.hasAlerts());
    }

    @Test
    void alertSummaryNoAlertsWhenClean() {
        var summary = new AlertingRules.AlertSummary(false, false, false);
        assertFalse(summary.hasAlerts());
    }
}
