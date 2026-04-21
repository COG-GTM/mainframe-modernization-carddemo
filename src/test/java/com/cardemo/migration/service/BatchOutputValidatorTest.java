package com.cardemo.migration.service;

import com.cardemo.migration.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BatchOutputValidator - validates batch job output comparison.
 */
class BatchOutputValidatorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Identical output files pass comparison")
    void identicalFilesPassed() throws IOException {
        Path ref = tempDir.resolve("reference.txt");
        Path mig = tempDir.resolve("migrated.txt");
        Files.writeString(ref, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(mig, "Line 1\nLine 2\nLine 3\n");

        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.compareOutputFiles(ref, mig);
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Different output files fail comparison")
    void differentFilesFailed() throws IOException {
        Path ref = tempDir.resolve("reference.txt");
        Path mig = tempDir.resolve("migrated.txt");
        Files.writeString(ref, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(mig, "Line 1\nDIFFERENT\nLine 3\n");

        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.compareOutputFiles(ref, mig);
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("Different line counts fail comparison")
    void differentLineCountsFailed() throws IOException {
        Path ref = tempDir.resolve("reference.txt");
        Path mig = tempDir.resolve("migrated.txt");
        Files.writeString(ref, "Line 1\nLine 2\n");
        Files.writeString(mig, "Line 1\nLine 2\nLine 3\n");

        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.compareOutputFiles(ref, mig);
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("Missing reference file fails")
    void missingReferenceFile() {
        Path ref = tempDir.resolve("nonexistent.txt");
        Path mig = tempDir.resolve("migrated.txt");
        try {
            Files.writeString(mig, "content");
        } catch (IOException e) {
            fail("Test setup failed");
        }

        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.compareOutputFiles(ref, mig);
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("Transaction counts match passes")
    void transactionCountsMatch() {
        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.validateTransactionCounts(1000, 1000);
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Transaction counts mismatch fails")
    void transactionCountsMismatch() {
        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.validateTransactionCounts(1000, 999);
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("Total amounts match with BigDecimal precision")
    void totalAmountsMatch() {
        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.validateTotalAmounts(
                new BigDecimal("123456789.99"),
                new BigDecimal("123456789.99"));
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Total amounts mismatch fails with diff details")
    void totalAmountsMismatch() {
        BatchOutputValidator validator = new BatchOutputValidator();
        ValidationResult result = validator.validateTotalAmounts(
                new BigDecimal("123456789.99"),
                new BigDecimal("123456789.98"));
        assertFalse(result.isPassed());
        assertTrue(result.getDetails().get(0).contains("0.01"));
    }

    @Test
    @DisplayName("Report summary with matching fields passes")
    void reportSummaryMatching() {
        BatchOutputValidator validator = new BatchOutputValidator();
        Map<String, String> reference = Map.of(
                "Total Transactions", "1000",
                "Total Amount", "50000.00",
                "Report Date", "2026-01-01"
        );
        Map<String, String> migrated = Map.of(
                "Total Transactions", "1000",
                "Total Amount", "50000.00",
                "Report Date", "2026-01-01"
        );

        ValidationResult result = validator.validateReportSummary(reference, migrated);
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Report summary with mismatching field fails")
    void reportSummaryMismatch() {
        BatchOutputValidator validator = new BatchOutputValidator();
        Map<String, String> reference = Map.of(
                "Total Transactions", "1000",
                "Total Amount", "50000.00"
        );
        Map<String, String> migrated = Map.of(
                "Total Transactions", "999",
                "Total Amount", "50000.00"
        );

        ValidationResult result = validator.validateReportSummary(reference, migrated);
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("Report summary with missing key in migrated output fails")
    void reportSummaryMissingKey() {
        BatchOutputValidator validator = new BatchOutputValidator();
        Map<String, String> reference = Map.of(
                "Total Transactions", "1000",
                "Total Amount", "50000.00"
        );
        Map<String, String> migrated = Map.of(
                "Total Transactions", "1000"
        );

        ValidationResult result = validator.validateReportSummary(reference, migrated);
        assertFalse(result.isPassed());
    }
}
