package com.cardemo.migration.service;

import com.cardemo.migration.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Validates batch job outputs by comparing results from migrated Java batch processing
 * against reference output from original COBOL batch execution.
 * Focuses on: transaction counts, total amounts, and report summaries.
 */
public class BatchOutputValidator {

    private static final Logger log = LoggerFactory.getLogger(BatchOutputValidator.class);

    /**
     * Compares two batch output files line-by-line for exact match.
     *
     * @param referenceOutput path to the COBOL reference output file
     * @param migratedOutput  path to the Java batch output file
     * @return validation result
     */
    public ValidationResult compareOutputFiles(Path referenceOutput, Path migratedOutput) {
        ValidationResult result = new ValidationResult(
                "BatchOutput:FileCompare",
                "Compare batch output files line-by-line");

        try {
            if (!Files.exists(referenceOutput)) {
                result.fail("Reference output file not found: " + referenceOutput);
                return result;
            }
            if (!Files.exists(migratedOutput)) {
                result.fail("Migrated output file not found: " + migratedOutput);
                return result;
            }

            List<String> refLines = Files.readAllLines(referenceOutput);
            List<String> migLines = Files.readAllLines(migratedOutput);

            if (refLines.size() != migLines.size()) {
                result.fail(String.format(
                        "Line count mismatch: reference=%d, migrated=%d",
                        refLines.size(), migLines.size()));
                return result;
            }

            int mismatches = 0;
            for (int i = 0; i < refLines.size(); i++) {
                if (!refLines.get(i).equals(migLines.get(i))) {
                    mismatches++;
                    if (mismatches <= 5) {
                        result.fail(String.format(
                                "Line %d mismatch: ref='%s', mig='%s'",
                                i + 1, refLines.get(i), migLines.get(i)));
                    }
                }
            }

            if (mismatches == 0) {
                result.pass(String.format(
                        "All %d lines match between reference and migrated output",
                        refLines.size()));
            } else if (mismatches > 5) {
                result.fail(String.format("Total mismatches: %d (showing first 5)", mismatches));
            }

        } catch (IOException e) {
            result.fail("Error reading output files: " + e.getMessage());
            log.error("Error during batch output comparison", e);
        }

        return result;
    }

    /**
     * Validates transaction count totals between reference and migrated batch output.
     *
     * @param referenceCount the transaction count from COBOL batch output
     * @param migratedCount  the transaction count from Java batch output
     * @return validation result
     */
    public ValidationResult validateTransactionCounts(long referenceCount, long migratedCount) {
        ValidationResult result = new ValidationResult(
                "BatchOutput:TransactionCount",
                "Compare transaction counts between reference and migrated output");

        if (referenceCount == migratedCount) {
            result.pass(String.format(
                    "Transaction counts match: %d", referenceCount));
        } else {
            result.fail(String.format(
                    "Transaction count mismatch: reference=%d, migrated=%d (diff=%d)",
                    referenceCount, migratedCount, referenceCount - migratedCount));
        }

        return result;
    }

    /**
     * Validates total monetary amounts between reference and migrated batch output.
     * Uses BigDecimal for precise comparison (no floating-point errors).
     *
     * @param referenceTotal the total amount from COBOL batch output
     * @param migratedTotal  the total amount from Java batch output
     * @return validation result
     */
    public ValidationResult validateTotalAmounts(BigDecimal referenceTotal, BigDecimal migratedTotal) {
        ValidationResult result = new ValidationResult(
                "BatchOutput:TotalAmounts",
                "Compare total amounts between reference and migrated output");

        if (referenceTotal.compareTo(migratedTotal) == 0) {
            result.pass(String.format(
                    "Total amounts match: %s", referenceTotal.toPlainString()));
        } else {
            BigDecimal diff = referenceTotal.subtract(migratedTotal).abs();
            result.fail(String.format(
                    "Total amount mismatch: reference=%s, migrated=%s (diff=%s)",
                    referenceTotal.toPlainString(),
                    migratedTotal.toPlainString(),
                    diff.toPlainString()));
        }

        return result;
    }

    /**
     * Validates report summary lines by comparing key-value pairs extracted
     * from batch report output.
     *
     * @param referenceSummary  key-value summary from COBOL output
     * @param migratedSummary   key-value summary from Java output
     * @return validation result
     */
    public ValidationResult validateReportSummary(
            java.util.Map<String, String> referenceSummary,
            java.util.Map<String, String> migratedSummary) {
        ValidationResult result = new ValidationResult(
                "BatchOutput:ReportSummary",
                "Compare report summary fields between reference and migrated output");

        boolean allMatch = true;
        for (var entry : referenceSummary.entrySet()) {
            String key = entry.getKey();
            String refValue = entry.getValue();
            String migValue = migratedSummary.get(key);

            if (migValue == null) {
                result.fail(String.format("Missing summary key in migrated output: %s", key));
                allMatch = false;
            } else if (!refValue.strip().equals(migValue.strip())) {
                result.fail(String.format(
                        "Summary mismatch for '%s': reference='%s', migrated='%s'",
                        key, refValue, migValue));
                allMatch = false;
            }
        }

        // Check for extra keys in migrated output
        for (String key : migratedSummary.keySet()) {
            if (!referenceSummary.containsKey(key)) {
                result.fail(String.format("Extra summary key in migrated output: %s", key));
                allMatch = false;
            }
        }

        if (allMatch) {
            result.pass(String.format("All %d summary fields match",
                    referenceSummary.size()));
        }

        return result;
    }
}
