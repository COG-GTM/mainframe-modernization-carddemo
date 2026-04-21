package com.cardemo.migration.service;

import com.cardemo.migration.model.CopybookField;
import com.cardemo.migration.model.CopybookLayout;
import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.model.VsamFileDescriptor;
import com.cardemo.migration.reader.CopybookLayouts;
import com.cardemo.migration.reader.EbcdicFieldParser;
import com.cardemo.migration.reader.EbcdicFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Performs field-level spot-check validation by reading specific records from EBCDIC files,
 * parsing them using copybook layouts, and comparing with database values.
 * Special attention is given to packed decimal (COMP-3) and signed numeric fields.
 */
public class FieldLevelValidator {

    private static final Logger log = LoggerFactory.getLogger(FieldLevelValidator.class);

    private final EbcdicFileReader fileReader;
    private final JdbcTemplate jdbcTemplate;
    private final int spotCheckCount;

    public FieldLevelValidator(EbcdicFileReader fileReader, JdbcTemplate jdbcTemplate, int spotCheckCount) {
        this.fileReader = fileReader;
        this.jdbcTemplate = jdbcTemplate;
        this.spotCheckCount = spotCheckCount;
    }

    /**
     * Validates field-level data by spot-checking records (first, last, and random).
     *
     * @param descriptor the VSAM file descriptor
     * @return validation result with details of each checked field
     */
    public ValidationResult validate(VsamFileDescriptor descriptor) {
        ValidationResult result = new ValidationResult(
                "FieldLevel:" + descriptor.name(),
                "Field-level spot check for " + descriptor.name());

        CopybookLayout layout = CopybookLayouts.getLayout(descriptor.copybookName());
        if (layout == null) {
            result.fail("No copybook layout found for " + descriptor.copybookName());
            return result;
        }

        try {
            int totalRecords = fileReader.countRecords(descriptor);
            List<Integer> indices = fileReader.getSpotCheckIndices(totalRecords, spotCheckCount);

            boolean allMatch = true;
            for (int idx : indices) {
                Map<String, String> parsedRecord = fileReader.readParsedRecord(descriptor, layout, idx);
                boolean recordMatch = compareWithDatabase(descriptor, layout, parsedRecord, idx, result);
                if (!recordMatch) {
                    allMatch = false;
                }
            }

            if (allMatch) {
                result.pass(String.format("All %d spot-checked records match", indices.size()));
            }

        } catch (IOException e) {
            result.fail("Error reading EBCDIC file: " + e.getMessage());
            log.error("Error during field-level validation for {}", descriptor.name(), e);
        }

        return result;
    }

    /**
     * Validates a single parsed EBCDIC record against expected values (for testing without DB).
     *
     * @param parsedRecord   the parsed field values from EBCDIC
     * @param expectedValues the expected values to compare against
     * @param layout         the copybook layout
     * @return validation result
     */
    public ValidationResult validateParsedRecord(Map<String, String> parsedRecord,
                                                  Map<String, String> expectedValues,
                                                  CopybookLayout layout) {
        ValidationResult result = new ValidationResult(
                "FieldLevel:Parsed",
                "Validate parsed record fields");

        boolean allMatch = true;
        for (CopybookField field : layout.dataFields()) {
            String ebcdicValue = parsedRecord.get(field.name());
            String expectedValue = expectedValues.get(field.columnName());

            if (expectedValue == null) {
                continue;
            }

            if (!valuesMatch(ebcdicValue, expectedValue, field)) {
                result.fail(String.format("Field %s mismatch: EBCDIC='%s', expected='%s'",
                        field.name(), ebcdicValue, expectedValue));
                allMatch = false;
            }
        }

        if (allMatch) {
            result.pass("All fields match");
        }

        return result;
    }

    /**
     * Compares parsed EBCDIC values with database row values.
     */
    private boolean compareWithDatabase(VsamFileDescriptor descriptor,
                                         CopybookLayout layout,
                                         Map<String, String> parsedRecord,
                                         int recordIndex,
                                         ValidationResult result) {
        try {
            String keyField = layout.dataFields().get(0).columnName();
            String keyValue = parsedRecord.get(layout.dataFields().get(0).name());

            String sql = "SELECT * FROM " + descriptor.tableName() +
                    " WHERE " + keyField + " = ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, keyValue);

            if (rows.isEmpty()) {
                result.fail(String.format("Record %d: no matching DB row for key %s=%s",
                        recordIndex, keyField, keyValue));
                return false;
            }

            Map<String, Object> dbRow = rows.get(0);
            boolean allMatch = true;
            for (CopybookField field : layout.dataFields()) {
                String ebcdicValue = parsedRecord.get(field.name());
                Object dbValue = dbRow.get(field.columnName());

                if (dbValue != null && !valuesMatch(ebcdicValue, dbValue.toString(), field)) {
                    result.fail(String.format(
                            "Record %d, field %s: EBCDIC='%s', DB='%s'",
                            recordIndex, field.name(), ebcdicValue, dbValue));
                    allMatch = false;
                }
            }

            return allMatch;
        } catch (Exception e) {
            result.fail(String.format("Record %d: DB comparison error: %s",
                    recordIndex, e.getMessage()));
            return false;
        }
    }

    /**
     * Compares an EBCDIC-parsed value with a database value, accounting for
     * numeric precision differences and whitespace trimming.
     */
    static boolean valuesMatch(String ebcdicValue, String dbValue, CopybookField field) {
        if (ebcdicValue == null && dbValue == null) {
            return true;
        }
        if (ebcdicValue == null || dbValue == null) {
            return false;
        }

        String normalizedEbcdic = ebcdicValue.strip();
        String normalizedDb = dbValue.strip();

        if (field.type() == CopybookField.FieldType.NUMERIC_DISPLAY ||
                field.type() == CopybookField.FieldType.PACKED_DECIMAL) {
            try {
                BigDecimal ebcdicNum = new BigDecimal(normalizedEbcdic);
                BigDecimal dbNum = new BigDecimal(normalizedDb);
                return ebcdicNum.compareTo(dbNum) == 0;
            } catch (NumberFormatException e) {
                return normalizedEbcdic.equals(normalizedDb);
            }
        }

        return normalizedEbcdic.equals(normalizedDb);
    }
}
