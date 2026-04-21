package com.cardemo.migration.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates multiple {@link ValidationResult}s into a comprehensive report.
 */
public class ValidationReport {

    private final String reportName;
    private final Instant startTime;
    private Instant endTime;
    private final List<ValidationResult> results;

    public ValidationReport(String reportName) {
        this.reportName = reportName;
        this.startTime = Instant.now();
        this.results = new ArrayList<>();
    }

    public void addResult(ValidationResult result) {
        results.add(result);
    }

    public void complete() {
        this.endTime = Instant.now();
    }

    public String getReportName() {
        return reportName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public List<ValidationResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    public long passedCount() {
        return results.stream()
                .filter(r -> r.getStatus() == ValidationResult.Status.PASSED)
                .count();
    }

    public long failedCount() {
        return results.stream()
                .filter(r -> r.getStatus() == ValidationResult.Status.FAILED)
                .count();
    }

    public long skippedCount() {
        return results.stream()
                .filter(r -> r.getStatus() == ValidationResult.Status.SKIPPED)
                .count();
    }

    public boolean isAllPassed() {
        return failedCount() == 0 && skippedCount() == 0;
    }

    public String getSummary() {
        return String.format(
                "Report: %s | Passed: %d | Failed: %d | Skipped: %d | Total: %d",
                reportName, passedCount(), failedCount(), skippedCount(), results.size());
    }
}
