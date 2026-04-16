package com.cardemo.migration.service;

import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.model.VsamFileDescriptor;
import com.cardemo.migration.reader.EbcdicFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RecordCountValidator - validates EBCDIC record counts
 * against expected values.
 */
class RecordCountValidatorTest {

    private static final Charset EBCDIC = Charset.forName("IBM037");

    @TempDir
    Path tempDir;

    private EbcdicFileReader fileReader;

    @BeforeEach
    void setUp() {
        fileReader = new EbcdicFileReader(tempDir);
    }

    private void createTestFile(String fileName, int recordLength, int recordCount) throws IOException {
        byte[] data = new byte[recordLength * recordCount];
        for (int i = 0; i < data.length; i++) {
            data[i] = 0x40; // EBCDIC space
        }
        Files.write(tempDir.resolve(fileName), data);
    }

    @Test
    @DisplayName("Record count matches expected count - PASSED")
    void recordCountMatchesExpected() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.ACCTDATA.PS", 300, 50);
        RecordCountValidator validator = new RecordCountValidator(fileReader, null);
        ValidationResult result = validator.validateAgainstExpected(VsamFileDescriptor.ACCTDATA, 50);
        assertTrue(result.isPassed());
        assertEquals(ValidationResult.Status.PASSED, result.getStatus());
    }

    @Test
    @DisplayName("Record count does not match expected count - FAILED")
    void recordCountMismatch() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.ACCTDATA.PS", 300, 50);
        RecordCountValidator validator = new RecordCountValidator(fileReader, null);
        ValidationResult result = validator.validateAgainstExpected(VsamFileDescriptor.ACCTDATA, 100);
        assertFalse(result.isPassed());
        assertEquals(ValidationResult.Status.FAILED, result.getStatus());
        assertTrue(result.getDetails().get(0).contains("mismatch"));
    }

    @Test
    @DisplayName("Validate CARDDATA file record count")
    void validateCardDataCount() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.CARDDATA.PS", 150, 50);
        RecordCountValidator validator = new RecordCountValidator(fileReader, null);
        ValidationResult result = validator.validateAgainstExpected(VsamFileDescriptor.CARDDATA, 50);
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Validate CARDXREF file record count")
    void validateCardXrefCount() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.CARDXREF.PS", 50, 50);
        RecordCountValidator validator = new RecordCountValidator(fileReader, null);
        ValidationResult result = validator.validateAgainstExpected(VsamFileDescriptor.CARDXREF, 50);
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Validate CUSTDATA file record count")
    void validateCustDataCount() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.CUSTDATA.PS", 500, 50);
        RecordCountValidator validator = new RecordCountValidator(fileReader, null);
        ValidationResult result = validator.validateAgainstExpected(VsamFileDescriptor.CUSTDATA, 50);
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Non-existent file returns FAILED result")
    void nonExistentFile() {
        RecordCountValidator validator = new RecordCountValidator(fileReader, null);
        VsamFileDescriptor missingFile = new VsamFileDescriptor(
                "MISSING", "NONEXISTENT.PS", 100, "missing", "NONE");
        ValidationResult result = validator.validateAgainstExpected(missingFile, 10);
        assertEquals(ValidationResult.Status.FAILED, result.getStatus());
    }
}
