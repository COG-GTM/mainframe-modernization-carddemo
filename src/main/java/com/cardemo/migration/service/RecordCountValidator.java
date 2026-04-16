package com.cardemo.migration.service;

import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.model.VsamFileDescriptor;
import com.cardemo.migration.reader.EbcdicFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Validates that the record count in each EBCDIC VSAM file matches
 * the row count in the corresponding PostgreSQL table.
 */
public class RecordCountValidator {

    private static final Logger log = LoggerFactory.getLogger(RecordCountValidator.class);

    private final EbcdicFileReader fileReader;
    private final JdbcTemplate jdbcTemplate;

    public RecordCountValidator(EbcdicFileReader fileReader, JdbcTemplate jdbcTemplate) {
        this.fileReader = fileReader;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Validates that the EBCDIC file record count matches the database table row count.
     *
     * @param descriptor the VSAM file descriptor
     * @return validation result indicating pass or fail with details
     */
    public ValidationResult validate(VsamFileDescriptor descriptor) {
        ValidationResult result = new ValidationResult(
                "RecordCount:" + descriptor.name(),
                "Verify record count for " + descriptor.name() +
                        " matches " + descriptor.tableName());

        try {
            int ebcdicCount = fileReader.countRecords(descriptor);
            int dbCount = countDatabaseRows(descriptor.tableName());

            if (ebcdicCount == dbCount) {
                result.pass(String.format(
                        "Record counts match: EBCDIC=%d, DB=%d", ebcdicCount, dbCount));
            } else {
                result.fail(String.format(
                        "Record count mismatch: EBCDIC=%d, DB=%d (diff=%d)",
                        ebcdicCount, dbCount, ebcdicCount - dbCount));
            }

            log.info("Record count validation for {}: EBCDIC={}, DB={}, status={}",
                    descriptor.name(), ebcdicCount, dbCount, result.getStatus());

        } catch (IOException e) {
            result.fail("Failed to read EBCDIC file: " + e.getMessage());
            log.error("Error reading EBCDIC file for {}", descriptor.name(), e);
        } catch (Exception e) {
            result.fail("Failed to query database: " + e.getMessage());
            log.error("Error querying database for {}", descriptor.name(), e);
        }

        return result;
    }

    /**
     * Counts rows in a database table using SELECT COUNT(*).
     *
     * @param tableName the PostgreSQL table name
     * @return the row count
     */
    int countDatabaseRows(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + sanitizeTableName(tableName);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Validates that the EBCDIC file record count matches an expected count
     * (useful for testing without a database).
     *
     * @param descriptor    the VSAM file descriptor
     * @param expectedCount the expected number of records
     * @return validation result
     */
    public ValidationResult validateAgainstExpected(VsamFileDescriptor descriptor, int expectedCount) {
        ValidationResult result = new ValidationResult(
                "RecordCount:" + descriptor.name(),
                "Verify record count for " + descriptor.name());

        try {
            int ebcdicCount = fileReader.countRecords(descriptor);

            if (ebcdicCount == expectedCount) {
                result.pass(String.format(
                        "Record count matches expected: %d", ebcdicCount));
            } else {
                result.fail(String.format(
                        "Record count mismatch: actual=%d, expected=%d",
                        ebcdicCount, expectedCount));
            }
        } catch (IOException e) {
            result.fail("Failed to read EBCDIC file: " + e.getMessage());
        }

        return result;
    }

    private static String sanitizeTableName(String tableName) {
        if (!tableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        return tableName;
    }
}
