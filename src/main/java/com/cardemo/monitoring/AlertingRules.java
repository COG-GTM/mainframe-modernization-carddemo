package com.cardemo.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Alerting rules for operational anomaly detection.
 *
 * Rules:
 * 1. Rejection rate > 10% of total transactions
 * 2. Job duration exceeds 2x average
 * 3. Zero transactions processed (potential data feed issue)
 */
@Component
public class AlertingRules {

    private static final Logger log = LoggerFactory.getLogger(AlertingRules.class);

    static final double REJECTION_RATE_THRESHOLD = 0.10;
    static final double JOB_DURATION_MULTIPLIER = 2.0;

    private final TransactionMetrics transactionMetrics;
    private final BatchJobMetrics batchJobMetrics;

    public AlertingRules(TransactionMetrics transactionMetrics, BatchJobMetrics batchJobMetrics) {
        this.transactionMetrics = transactionMetrics;
        this.batchJobMetrics = batchJobMetrics;
    }

    /**
     * Check if the rejection rate exceeds the threshold (10%).
     *
     * @return true if an alert should fire
     */
    public boolean checkRejectionRate() {
        double processed = transactionMetrics.getTransactionsProcessedCount();
        double rejected = transactionMetrics.getTransactionsRejectedCount();

        if (processed == 0 && rejected == 0) {
            return false;
        }

        double total = processed + rejected;
        double rejectionRate = rejected / total;

        if (rejectionRate > REJECTION_RATE_THRESHOLD) {
            log.warn("ALERT: Rejection rate {}% exceeds threshold of {}%. "
                            + "Processed={}, Rejected={}",
                    String.format("%.2f", rejectionRate * 100),
                    String.format("%.0f", REJECTION_RATE_THRESHOLD * 100),
                    (long) processed, (long) rejected);
            return true;
        }
        return false;
    }

    /**
     * Check if the rejection rate from explicit counts exceeds the threshold.
     */
    public boolean checkRejectionRate(long processed, long rejected) {
        if (processed == 0 && rejected == 0) {
            return false;
        }

        double total = processed + rejected;
        double rejectionRate = rejected / total;

        if (rejectionRate > REJECTION_RATE_THRESHOLD) {
            log.warn("ALERT: Rejection rate {}% exceeds threshold of {}%. "
                            + "Processed={}, Rejected={}",
                    String.format("%.2f", rejectionRate * 100),
                    String.format("%.0f", REJECTION_RATE_THRESHOLD * 100),
                    processed, rejected);
            return true;
        }
        return false;
    }

    /**
     * Check if job duration exceeds 2x the average.
     *
     * @param currentDurationMs duration of the current job in milliseconds
     * @return true if an alert should fire
     */
    public boolean checkJobDurationAnomaly(double currentDurationMs) {
        double averageDurationMs = batchJobMetrics.getJobMeanDurationMs();

        if (averageDurationMs <= 0) {
            return false;
        }

        double threshold = averageDurationMs * JOB_DURATION_MULTIPLIER;

        if (currentDurationMs > threshold) {
            log.warn("ALERT: Job duration {}ms exceeds 2x average ({}ms). Threshold={}ms",
                    String.format("%.0f", currentDurationMs),
                    String.format("%.0f", averageDurationMs),
                    String.format("%.0f", threshold));
            return true;
        }
        return false;
    }

    /**
     * Check if zero transactions were processed (potential data feed issue).
     *
     * @return true if an alert should fire
     */
    public boolean checkZeroTransactions() {
        double processed = transactionMetrics.getTransactionsProcessedCount();

        if (processed == 0) {
            log.warn("ALERT: Zero transactions processed - potential data feed issue");
            return true;
        }
        return false;
    }

    /**
     * Check zero transactions with explicit count.
     */
    public boolean checkZeroTransactions(long processedCount) {
        if (processedCount == 0) {
            log.warn("ALERT: Zero transactions processed - potential data feed issue");
            return true;
        }
        return false;
    }

    /**
     * Run all alerting checks and return a summary.
     */
    public AlertSummary evaluateAll() {
        boolean rejectionAlert = checkRejectionRate();
        boolean zeroTransactionsAlert = checkZeroTransactions();

        return new AlertSummary(rejectionAlert, false, zeroTransactionsAlert);
    }

    public record AlertSummary(
            boolean rejectionRateExceeded,
            boolean jobDurationExceeded,
            boolean zeroTransactionsDetected
    ) {
        public boolean hasAlerts() {
            return rejectionRateExceeded || jobDurationExceeded || zeroTransactionsDetected;
        }
    }
}
