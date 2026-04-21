package com.cardemo.migration.validation;

import com.cardemo.migration.model.ValidationReport;
import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.model.VsamFileDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ValidationResult, ValidationReport, and VsamFileDescriptor models.
 */
class ValidationModelTest {

    // ======================== ValidationResult Tests ========================

    @Test
    @DisplayName("New ValidationResult starts as SKIPPED")
    void newResultIsSkipped() {
        ValidationResult result = new ValidationResult("test", "description");
        assertEquals(ValidationResult.Status.SKIPPED, result.getStatus());
        assertFalse(result.isPassed());
    }

    @Test
    @DisplayName("ValidationResult pass sets PASSED status")
    void resultPassSetsStatus() {
        ValidationResult result = new ValidationResult("test", "description");
        result.pass("success detail");
        assertTrue(result.isPassed());
        assertEquals(1, result.getDetails().size());
    }

    @Test
    @DisplayName("ValidationResult fail sets FAILED status")
    void resultFailSetsStatus() {
        ValidationResult result = new ValidationResult("test", "description");
        result.fail("failure detail");
        assertFalse(result.isPassed());
        assertEquals(ValidationResult.Status.FAILED, result.getStatus());
    }

    @Test
    @DisplayName("ValidationResult toString includes relevant info")
    void resultToString() {
        ValidationResult result = new ValidationResult("RecordCount:ACCTDATA", "test");
        result.pass("50 records match");
        String str = result.toString();
        assertTrue(str.contains("RecordCount:ACCTDATA"));
        assertTrue(str.contains("PASSED"));
    }

    // ======================== ValidationReport Tests ========================

    @Test
    @DisplayName("Empty report has zero counts")
    void emptyReportZeroCounts() {
        ValidationReport report = new ValidationReport("test");
        assertEquals(0, report.passedCount());
        assertEquals(0, report.failedCount());
        assertEquals(0, report.skippedCount());
    }

    @Test
    @DisplayName("Report counts PASSED, FAILED, SKIPPED correctly")
    void reportCountsCorrectly() {
        ValidationReport report = new ValidationReport("test");

        ValidationResult passed = new ValidationResult("p", "p");
        passed.pass();
        report.addResult(passed);

        ValidationResult failed = new ValidationResult("f", "f");
        failed.fail("failed");
        report.addResult(failed);

        ValidationResult skipped = new ValidationResult("s", "s");
        report.addResult(skipped);

        assertEquals(1, report.passedCount());
        assertEquals(1, report.failedCount());
        assertEquals(1, report.skippedCount());
        assertFalse(report.isAllPassed());
    }

    @Test
    @DisplayName("Report isAllPassed true when no failures or skips")
    void reportAllPassedTrue() {
        ValidationReport report = new ValidationReport("test");
        ValidationResult r1 = new ValidationResult("r1", "r1");
        r1.pass();
        ValidationResult r2 = new ValidationResult("r2", "r2");
        r2.pass();
        report.addResult(r1);
        report.addResult(r2);
        assertTrue(report.isAllPassed());
    }

    @Test
    @DisplayName("Report summary contains counts")
    void reportSummaryContainsCounts() {
        ValidationReport report = new ValidationReport("Migration Test");
        ValidationResult r1 = new ValidationResult("r1", "r1");
        r1.pass();
        report.addResult(r1);
        report.complete();

        String summary = report.getSummary();
        assertTrue(summary.contains("Migration Test"));
        assertTrue(summary.contains("Passed: 1"));
    }

    @Test
    @DisplayName("Report complete sets endTime")
    void reportCompleteSetsEndTime() {
        ValidationReport report = new ValidationReport("test");
        assertNull(report.getEndTime());
        report.complete();
        assertNotNull(report.getEndTime());
    }

    // ======================== VsamFileDescriptor Tests ========================

    @Test
    @DisplayName("VsamFileDescriptor ALL array contains 11 entries (TRANDATA excluded)")
    void allDescriptorsCount() {
        assertEquals(11, VsamFileDescriptor.ALL.length);
    }

    @Test
    @DisplayName("ACCTDATA descriptor has correct properties")
    void acctdataDescriptor() {
        VsamFileDescriptor desc = VsamFileDescriptor.ACCTDATA;
        assertEquals("ACCTDATA", desc.name());
        assertEquals("AWS.M2.CARDDEMO.ACCTDATA.PS", desc.fileName());
        assertEquals(300, desc.recordLength());
        assertEquals("accounts", desc.tableName());
        assertEquals("CVACT01Y", desc.copybookName());
    }

    @Test
    @DisplayName("CARDXREF descriptor has correct properties")
    void cardxrefDescriptor() {
        VsamFileDescriptor desc = VsamFileDescriptor.CARDXREF;
        assertEquals("CARDXREF", desc.name());
        assertEquals(50, desc.recordLength());
        assertEquals("card_xrefs", desc.tableName());
    }

    @Test
    @DisplayName("CUSTDATA descriptor has correct properties")
    void custdataDescriptor() {
        VsamFileDescriptor desc = VsamFileDescriptor.CUSTDATA;
        assertEquals("CUSTDATA", desc.name());
        assertEquals(500, desc.recordLength());
        assertEquals("customers", desc.tableName());
    }

    @Test
    @DisplayName("USRSEC descriptor has correct properties")
    void usrsecDescriptor() {
        VsamFileDescriptor desc = VsamFileDescriptor.USRSEC;
        assertEquals("USRSEC", desc.name());
        assertEquals(80, desc.recordLength());
        assertEquals("user_security", desc.tableName());
    }
}
