package com.cardemo.batch;

import com.cardemo.batch.model.ReportLine;
import com.cardemo.batch.service.ReportFormatterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReportFormatterService verifying CVTRA07Y report structures
 * and the 133-byte record format.
 */
class ReportFormatterServiceTest {

    private static final int RECORD_LENGTH = 133;

    private ReportFormatterService formatter;

    @BeforeEach
    void setUp() {
        formatter = new ReportFormatterService();
    }

    @Test
    void reportNameHeader_shouldBe133Bytes() {
        ReportLine line = formatter.formatReportNameHeader("2022-01-01", "2022-12-31");
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.REPORT_NAME_HEADER, line.getLineType());
        assertTrue(line.getContent().contains("DALYREPT"));
        assertTrue(line.getContent().contains("Daily Transaction Report"));
        assertTrue(line.getContent().contains("Date Range: "));
        assertTrue(line.getContent().contains("2022-01-01"));
        assertTrue(line.getContent().contains("2022-12-31"));
    }

    @Test
    void transactionHeader1_shouldBe133Bytes() {
        ReportLine line = formatter.formatTransactionHeader1();
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.TRANSACTION_HEADER_1, line.getLineType());
        assertTrue(line.getContent().contains("Transaction ID"));
        assertTrue(line.getContent().contains("Account ID"));
        assertTrue(line.getContent().contains("Transaction Type"));
        assertTrue(line.getContent().contains("Tran Category"));
        assertTrue(line.getContent().contains("Tran Source"));
        assertTrue(line.getContent().contains("Amount"));
    }

    @Test
    void transactionHeader2_shouldBe133BytesOfDashes() {
        ReportLine line = formatter.formatTransactionHeader2();
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.TRANSACTION_HEADER_2, line.getLineType());
        assertEquals("-".repeat(RECORD_LENGTH), line.getContent());
    }

    @Test
    void blankLine_shouldBe133BytesOfSpaces() {
        ReportLine line = formatter.formatBlankLine();
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.BLANK_LINE, line.getLineType());
        assertEquals(" ".repeat(RECORD_LENGTH), line.getContent());
    }

    @Test
    void detailLine_shouldBe133Bytes() {
        ReportLine line = formatter.formatDetailLine(
                "0000000000000001",
                "00012345678",
                "SA",
                "Sale",
                5001,
                "Retail Purchase",
                "ONLINE",
                new BigDecimal("125.50")
        );
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.DETAIL, line.getLineType());
        assertTrue(line.getContent().contains("0000000000000001"));
        assertTrue(line.getContent().contains("00012345678"));
        assertTrue(line.getContent().contains("SA"));
        assertTrue(line.getContent().contains("Sale"));
        assertTrue(line.getContent().contains("5001"));
        assertTrue(line.getContent().contains("Retail Purchase"));
        assertTrue(line.getContent().contains("ONLINE"));
        assertTrue(line.getContent().contains("125.50"));
    }

    @Test
    void detailLine_negativeAmount_shouldShowMinus() {
        ReportLine line = formatter.formatDetailLine(
                "0000000000000002",
                "00012345678",
                "CR",
                "Credit",
                1001,
                "Return",
                "POS",
                new BigDecimal("-50.00")
        );
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertTrue(line.getContent().contains("-50.00"));
    }

    @Test
    void pageTotals_shouldBe133Bytes() {
        ReportLine line = formatter.formatPageTotals(new BigDecimal("1234.56"));
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.PAGE_TOTALS, line.getLineType());
        assertTrue(line.getContent().startsWith("Page Total"));
        assertTrue(line.getContent().contains("..."));
        assertTrue(line.getContent().contains("1,234.56"));
    }

    @Test
    void accountTotals_shouldBe133Bytes() {
        ReportLine line = formatter.formatAccountTotals(new BigDecimal("9876.54"));
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.ACCOUNT_TOTALS, line.getLineType());
        assertTrue(line.getContent().startsWith("Account Total"));
        assertTrue(line.getContent().contains("..."));
        assertTrue(line.getContent().contains("9,876.54"));
    }

    @Test
    void grandTotals_shouldBe133Bytes() {
        ReportLine line = formatter.formatGrandTotals(new BigDecimal("50000.00"));
        assertEquals(RECORD_LENGTH, line.getContent().length());
        assertEquals(ReportLine.LineType.GRAND_TOTALS, line.getLineType());
        assertTrue(line.getContent().startsWith("Grand Total"));
        assertTrue(line.getContent().contains("..."));
        assertTrue(line.getContent().contains("50,000.00"));
    }

    @Test
    void formatAmount_positiveAmount() {
        String result = formatter.formatAmount(new BigDecimal("123456789.12"));
        assertEquals(15, result.length());
        assertTrue(result.contains("123,456,789.12"));
    }

    @Test
    void formatAmount_negativeAmount() {
        String result = formatter.formatAmount(new BigDecimal("-100.00"));
        assertEquals(15, result.length());
        assertTrue(result.contains("-100.00"));
    }

    @Test
    void formatSignedAmount_positiveShowsPlus() {
        String result = formatter.formatSignedAmount(new BigDecimal("500.00"));
        assertEquals(15, result.length());
        assertTrue(result.contains("+500.00"));
    }

    @Test
    void formatSignedAmount_negativeShowsMinus() {
        String result = formatter.formatSignedAmount(new BigDecimal("-500.00"));
        assertEquals(15, result.length());
        assertTrue(result.contains("-500.00"));
    }

    @Test
    void formatSignedAmount_zero() {
        String result = formatter.formatSignedAmount(BigDecimal.ZERO);
        assertEquals(15, result.length());
        assertTrue(result.contains("+0.00"));
    }
}
