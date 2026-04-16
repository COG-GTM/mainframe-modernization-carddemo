package com.cardemo.batch.formatter;

import com.cardemo.batch.TestDataFactory;
import com.cardemo.batch.service.StatementData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for PlainTextStatementFormatter.
 * Validates output matches CBSTM03A STATEMENT-LINES structure.
 */
class PlainTextStatementFormatterTest {

    private PlainTextStatementFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new PlainTextStatementFormatter();
    }

    @Test
    void format_producesCorrectStructure() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        assertNotNull(lines);
        assertFalse(lines.isEmpty());
        // First line: START OF STATEMENT banner
        assertTrue(lines.get(0).contains("START OF STATEMENT"));
        // Last line: END OF STATEMENT banner
        assertTrue(lines.get(lines.size() - 1).contains("END OF STATEMENT"));
    }

    @Test
    void format_includesCustomerName() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        // ST-LINE1: customer name
        assertTrue(lines.get(1).contains("John"));
        assertTrue(lines.get(1).contains("Doe"));
    }

    @Test
    void format_includesAddressLines() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        // ST-LINE2: address line 1
        assertTrue(lines.get(2).contains("123 Main St"));
        // ST-LINE3: address line 2
        assertTrue(lines.get(3).contains("Apt 4B"));
        // ST-LINE4: address line 3 + state + country + zip
        assertTrue(lines.get(4).contains("Downtown"));
        assertTrue(lines.get(4).contains("WA"));
    }

    @Test
    void format_includesBasicDetails() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String allText = String.join("\n", lines);
        assertTrue(allText.contains("Basic Details"));
        assertTrue(allText.contains("Account ID"));
        assertTrue(allText.contains("Current Balance"));
        assertTrue(allText.contains("FICO Score"));
        assertTrue(allText.contains("750"));
    }

    @Test
    void format_includesTransactionSummary() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String allText = String.join("\n", lines);
        assertTrue(allText.contains("TRANSACTION SUMMARY"));
        assertTrue(allText.contains("Tran ID"));
        assertTrue(allText.contains("Tran Details"));
        assertTrue(allText.contains("Tran Amount"));
    }

    @Test
    void format_includesTransactionDetails() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String allText = String.join("\n", lines);
        assertTrue(allText.contains("Grocery Store Purchase"));
        assertTrue(allText.contains("Gas Station"));
        assertTrue(allText.contains("Restaurant Dinner"));
    }

    @Test
    void format_includesTotalLine() {
        StatementData data = TestDataFactory.createStatementData();

        List<String> lines = formatter.format(data);

        String allText = String.join("\n", lines);
        assertTrue(allText.contains("Total EXP:"));
        assertTrue(allText.contains("156.27"));
    }

    @Test
    void buildCustomerName_concatenatesCorrectly() {
        String name = formatter.buildCustomerName("John", "M", "Doe");
        assertEquals("John M Doe", name);
    }

    @Test
    void formatAmount_handlesNegativeValues() {
        String result = formatter.formatAmount(new BigDecimal("-45.50"));
        assertTrue(result.contains("45.50"));
        assertTrue(result.endsWith("-"));
    }

    @Test
    void formatAmount_handlesPositiveValues() {
        String result = formatter.formatAmount(new BigDecimal("100.00"));
        assertTrue(result.contains("100.00"));
        assertFalse(result.endsWith("-"));
    }
}
