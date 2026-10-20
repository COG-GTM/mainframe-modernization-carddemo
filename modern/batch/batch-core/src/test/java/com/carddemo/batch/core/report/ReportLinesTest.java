package com.carddemo.batch.core.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carddemo.batch.core.FixedWidth;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReportLinesTest {

    private static String text(byte[] line) {
        assertEquals(ReportLines.LENGTH, line.length);
        return FixedWidth.decode(line);
    }

    @Test
    void nameHeader() {
        String line = text(new ReportNameHeader("2022-01-01", "2022-07-06").toBytes());
        assertEquals(FixedWidth.fit("DALYREPT", 38) + FixedWidth.fit("Daily Transaction Report", 41)
                + "Date Range: 2022-01-01 to 2022-07-06" + " ".repeat(18), line);
        assertEquals(new ReportNameHeader("2022-01-01", "2022-07-06"),
                ReportNameHeader.fromBytes(FixedWidth.encode(line)));
    }

    @Test
    void constantLines() {
        assertEquals("-".repeat(133), text(ReportLines.transactionHeader2()));
        assertEquals(" ".repeat(133), text(ReportLines.blankLine()));
        String header1 = text(ReportLines.transactionHeader1());
        assertEquals("Transaction ID   Account ID  Transaction Type   Tran Category"
                + " ".repeat(22) + "Tran Source            Amount", header1.stripTrailing());
        assertEquals(112, header1.stripTrailing().length());
    }

    @Test
    void detailLine() {
        TransactionDetailLine detail = new TransactionDetailLine("0000000000683580", "00000000005", "01",
                "Purchase", 1, "Regular Sales Draft", "POS TERM", new BigDecimal("-1234.5"));
        String line = text(detail.toBytes());
        assertEquals("0000000000683580 00000000005 01-Purchase        0001-Regular Sales Draft           "
                + "POS TERM      -      1,234.50" + " ".repeat(21), line);
        assertEquals(97, line.indexOf("-      1,234.50"));
        assertEquals(detail, TransactionDetailLine.fromBytes(detail.toBytes()));
    }

    @Test
    void detailLineTruncatesDescriptions() {
        TransactionDetailLine detail = new TransactionDetailLine("1", "2", "03", "Credit card refund long",
                5, "A category description that is too long", "System", BigDecimal.ZERO);
        String line = text(detail.toBytes());
        assertEquals("Credit card ref", line.substring(32, 47));
        assertEquals("A category description that i", line.substring(53, 82));
        assertEquals(" ".repeat(15), line.substring(97, 112));
    }

    @Test
    void totals() {
        assertEquals("Page Total " + ".".repeat(86) + "+      1,000.00" + " ".repeat(21),
                text(new ReportPageTotals(new BigDecimal("1000")).toBytes()));
        assertEquals("Account Total" + ".".repeat(84) + "-        123.45" + " ".repeat(21),
                text(new ReportAccountTotals(new BigDecimal("-123.45")).toBytes()));
        assertEquals("Grand Total" + ".".repeat(86) + " ".repeat(15) + " ".repeat(21),
                text(new ReportGrandTotals(BigDecimal.ZERO).toBytes()));
        ReportGrandTotals grand = new ReportGrandTotals(new BigDecimal("98765.43"));
        assertEquals(grand, ReportGrandTotals.fromBytes(grand.toBytes()));
    }
}
