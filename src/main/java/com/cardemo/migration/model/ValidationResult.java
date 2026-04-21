package com.cardemo.migration.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Captures the outcome of a migration validation check.
 */
public class ValidationResult {

    public enum Status {
        PASSED, FAILED, SKIPPED
    }

    private final String checkName;
    private final String description;
    private Status status;
    private final List<String> details;
    private final Instant timestamp;

    public ValidationResult(String checkName, String description) {
        this.checkName = checkName;
        this.description = description;
        this.status = Status.SKIPPED;
        this.details = new ArrayList<>();
        this.timestamp = Instant.now();
    }

    public void pass() {
        this.status = Status.PASSED;
    }

    public void pass(String detail) {
        this.status = Status.PASSED;
        this.details.add(detail);
    }

    public void fail(String detail) {
        this.status = Status.FAILED;
        this.details.add(detail);
    }

    public String getCheckName() {
        return checkName;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getDetails() {
        return Collections.unmodifiableList(details);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isPassed() {
        return status == Status.PASSED;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s - %s",
                timestamp, checkName, status, String.join("; ", details));
    }
}
