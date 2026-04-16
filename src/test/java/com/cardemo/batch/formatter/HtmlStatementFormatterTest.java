package com.cardemo.batch.formatter;

import com.cardemo.batch.TestDataFactory;
import com.cardemo.batch.service.StatementData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for HtmlStatementFormatter.
 * Validates output matches CBSTM03A HTML-LINES structure with inline CSS.
 */
class HtmlStatementFormatterTest {

    private HtmlStatementFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new HtmlStatementFormatter();
    }

    @Test
    void format_producesValidHtmlDocument() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        assertNotNull(lines);
        String html = String.join("\n", lines);
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<html lang=\"en\">"));
        assertTrue(html.contains("</html>"));
    }

    @Test
    void format_includesBankHeader() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String html = String.join("\n", lines);
        // HTML-L16: Bank name
        assertTrue(html.contains("Bank of XYZ"));
        // HTML-L17: Bank address
        assertTrue(html.contains("410 Terry Ave N"));
        // HTML-L18: Bank city/state/zip
        assertTrue(html.contains("Seattle WA 99999"));
    }

    @Test
    void format_includesAccountNumber() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String html = String.join("\n", lines);
        // HTML-L11: Statement for Account Number
        assertTrue(html.contains("Statement for Account Number:"));
        assertTrue(html.contains("00000000001"));
    }

    @Test
    void format_includesInlineCssStyles() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String html = String.join("\n", lines);
        // Verify key CSS styles from COBOL 88-level values
        assertTrue(html.contains("background-color:#1d1d96b3"));
        assertTrue(html.contains("background-color:#FFAF33"));
        assertTrue(html.contains("background-color:#f2f2f2"));
        assertTrue(html.contains("background-color:#33FFD1"));
        assertTrue(html.contains("background-color:#33FF5E"));
    }

    @Test
    void format_includesCustomerDetails() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String html = String.join("\n", lines);
        assertTrue(html.contains("John"));
        assertTrue(html.contains("Doe"));
        assertTrue(html.contains("123 Main St"));
    }

    @Test
    void format_includesBasicDetails() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String html = String.join("\n", lines);
        assertTrue(html.contains("Basic Details"));
        assertTrue(html.contains("Account ID"));
        assertTrue(html.contains("Current Balance"));
        assertTrue(html.contains("FICO Score"));
    }

    @Test
    void format_includesTransactionRows() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String html = String.join("\n", lines);
        assertTrue(html.contains("Transaction Summary"));
        assertTrue(html.contains("Tran ID"));
        assertTrue(html.contains("Tran Details"));
        assertTrue(html.contains("Amount"));
        assertTrue(html.contains("Grocery Store Purchase"));
        assertTrue(html.contains("Gas Station"));
        assertTrue(html.contains("Restaurant Dinner"));
    }

    @Test
    void format_includesEndOfStatement() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String html = String.join("\n", lines);
        // HTML-L75
        assertTrue(html.contains("End of Statement"));
    }

    @Test
    void format_handlesNegativeBalance() {
        StatementData data = TestDataFactory.createStatementDataWithNegativeAmount();

        List<String> lines = formatter.format(data);

        assertNotNull(lines);
        assertFalse(lines.isEmpty());
        String html = String.join("\n", lines);
        assertTrue(html.contains("-250.75"));
    }
}
