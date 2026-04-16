package com.cardemo.batch;

import com.cardemo.batch.model.CardXrefRecord;
import com.cardemo.batch.model.TranCategoryRecord;
import com.cardemo.batch.model.TranTypeRecord;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.service.ReferenceDataService;
import com.cardemo.batch.service.ReportFormatterService;
import com.cardemo.batch.writer.TransactionReportWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for TransactionReportWriter verifying the complete report generation logic
 * including page breaks, account breaks, and all three levels of totals.
 */
@ExtendWith(MockitoExtension.class)
class TransactionReportWriterTest {

    @Mock
    private ReferenceDataService referenceDataService;

    private ReportFormatterService formatter;
    private StringWriter stringWriter;
    private BufferedWriter bufferedWriter;
    private TransactionReportWriter reportWriter;

    @BeforeEach
    void setUp() {
        formatter = new ReportFormatterService();
        stringWriter = new StringWriter();
        bufferedWriter = new BufferedWriter(stringWriter);
        reportWriter = new TransactionReportWriter(bufferedWriter, formatter, referenceDataService,
                "2022-01-01", "2022-12-31", 20);

        when(referenceDataService.lookupCardXref(anyString()))
                .thenReturn(new CardXrefRecord("1234567890123456", 1L, "00012345678"));
        when(referenceDataService.lookupTranType(anyString()))
                .thenReturn(new TranTypeRecord("SA", "Sale"));
        when(referenceDataService.lookupTranCategory(anyString(), anyInt()))
                .thenReturn(new TranCategoryRecord("SA", 5001, "Retail Purchase"));
    }

    @Test
    void write_singleTransaction_shouldWriteHeadersAndDetail() throws Exception {
        TransactionRecord record = createRecord("0000000000000001", "1234567890123456",
                "SA", 5001, new BigDecimal("100.00"));

        reportWriter.write(new Chunk<>(List.of(record)));
        reportWriter.writeClosingTotals();

        String output = stringWriter.toString();
        // Should contain report header
        assertTrue(output.contains("DALYREPT"));
        assertTrue(output.contains("Daily Transaction Report"));
        // Should contain column headers
        assertTrue(output.contains("Transaction ID"));
        assertTrue(output.contains("Account ID"));
        // Should contain detail line
        assertTrue(output.contains("0000000000000001"));
        assertTrue(output.contains("00012345678"));
        // Should contain totals
        assertTrue(output.contains("Page Total"));
        assertTrue(output.contains("Grand Total"));
        // All lines should be 133 chars
        for (String line : output.split("\n")) {
            assertEquals(133, line.length(),
                    "Line not 133 bytes: '" + line + "'");
        }
    }

    @Test
    void write_multipleTransactions_sameTotals() throws Exception {
        TransactionRecord r1 = createRecord("0000000000000001", "1234567890123456",
                "SA", 5001, new BigDecimal("100.00"));
        TransactionRecord r2 = createRecord("0000000000000002", "1234567890123456",
                "SA", 5001, new BigDecimal("250.00"));

        reportWriter.write(new Chunk<>(List.of(r1, r2)));

        // Page total should be 350.00 (accumulated)
        assertEquals(new BigDecimal("350.00"), reportWriter.getPageTotal());
        // Account total should be 350.00
        assertEquals(new BigDecimal("350.00"), reportWriter.getAccountTotal());
    }

    @Test
    void write_cardNumberChange_shouldWriteAccountTotals() throws Exception {
        TransactionRecord r1 = createRecord("0000000000000001", "1111111111111111",
                "SA", 5001, new BigDecimal("100.00"));
        TransactionRecord r2 = createRecord("0000000000000002", "2222222222222222",
                "SA", 5001, new BigDecimal("200.00"));

        when(referenceDataService.lookupCardXref("1111111111111111"))
                .thenReturn(new CardXrefRecord("1111111111111111", 1L, "00011111111"));
        when(referenceDataService.lookupCardXref("2222222222222222"))
                .thenReturn(new CardXrefRecord("2222222222222222", 2L, "00022222222"));

        reportWriter.write(new Chunk<>(List.of(r1, r2)));
        bufferedWriter.flush();

        String output = stringWriter.toString();
        // Should contain account totals for the first card
        assertTrue(output.contains("Account Total"));
        // Account total should be reset to 200.00 (only second card's amount)
        assertEquals(new BigDecimal("200.00"), reportWriter.getAccountTotal());
    }

    @Test
    void write_pageBreak_shouldWritePageTotalsAndHeaders() throws Exception {
        // Create writer with page size of 3 for easier testing
        stringWriter = new StringWriter();
        bufferedWriter = new BufferedWriter(stringWriter);
        reportWriter = new TransactionReportWriter(bufferedWriter, formatter, referenceDataService,
                "2022-01-01", "2022-12-31", 3);

        when(referenceDataService.lookupCardXref(anyString()))
                .thenReturn(new CardXrefRecord("1234567890123456", 1L, "00012345678"));
        when(referenceDataService.lookupTranType(anyString()))
                .thenReturn(new TranTypeRecord("SA", "Sale"));
        when(referenceDataService.lookupTranCategory(anyString(), anyInt()))
                .thenReturn(new TranCategoryRecord("SA", 5001, "Retail Purchase"));

        // Write enough transactions to trigger page break.
        // First page: 4 header lines + detail lines. lineCounter starts at 0.
        // After headers: lineCounter = 4. Detail lines at 4, 5, 6...
        // At lineCounter=6 (mod 3 = 0), page break triggers.
        // Let's write several transactions.
        for (int i = 1; i <= 5; i++) {
            TransactionRecord r = createRecord(
                    String.format("%016d", i), "1234567890123456",
                    "SA", 5001, new BigDecimal("100.00"));
            reportWriter.write(new Chunk<>(List.of(r)));
        }

        bufferedWriter.flush();
        String output = stringWriter.toString();
        // Should contain at least one page total from page break
        assertTrue(output.contains("Page Total"));
    }

    @Test
    void write_closingTotals_shouldWriteGrandTotal() throws Exception {
        TransactionRecord r1 = createRecord("0000000000000001", "1234567890123456",
                "SA", 5001, new BigDecimal("100.00"));

        reportWriter.write(new Chunk<>(List.of(r1)));
        reportWriter.writeClosingTotals();

        String output = stringWriter.toString();
        assertTrue(output.contains("Grand Total"));
        assertTrue(output.contains("Page Total"));
    }

    @Test
    void write_allLinesShouldBe133Bytes() throws Exception {
        TransactionRecord r1 = createRecord("0000000000000001", "1111111111111111",
                "SA", 5001, new BigDecimal("100.00"));
        TransactionRecord r2 = createRecord("0000000000000002", "2222222222222222",
                "CR", 1001, new BigDecimal("-50.00"));

        when(referenceDataService.lookupCardXref("1111111111111111"))
                .thenReturn(new CardXrefRecord("1111111111111111", 1L, "00011111111"));
        when(referenceDataService.lookupCardXref("2222222222222222"))
                .thenReturn(new CardXrefRecord("2222222222222222", 2L, "00022222222"));
        when(referenceDataService.lookupTranType("CR"))
                .thenReturn(new TranTypeRecord("CR", "Credit"));
        when(referenceDataService.lookupTranCategory("CR", 1001))
                .thenReturn(new TranCategoryRecord("CR", 1001, "Return"));

        reportWriter.write(new Chunk<>(List.of(r1, r2)));
        reportWriter.writeClosingTotals();

        String output = stringWriter.toString();
        String[] lines = output.split("\n");
        for (String line : lines) {
            assertEquals(133, line.length(),
                    "Line not 133 bytes: '" + line + "'");
        }
    }

    @Test
    void write_referenceLookups_shouldBeCalledCorrectly() throws Exception {
        TransactionRecord r = createRecord("0000000000000001", "1234567890123456",
                "SA", 5001, new BigDecimal("100.00"));

        reportWriter.write(new Chunk<>(List.of(r)));

        verify(referenceDataService).lookupCardXref("1234567890123456");
        verify(referenceDataService).lookupTranType("SA");
        verify(referenceDataService).lookupTranCategory("SA", 5001);
    }

    private TransactionRecord createRecord(String tranId, String cardNum,
                                           String typeCd, int catCd, BigDecimal amount) {
        TransactionRecord record = new TransactionRecord();
        record.setTranId(tranId);
        record.setTranTypeCd(typeCd);
        record.setTranCatCd(catCd);
        record.setTranSource("ONLINE");
        record.setTranDesc("Test transaction");
        record.setTranAmt(amount);
        record.setTranCardNum(cardNum);
        record.setTranProcTs("2022-06-15T10:30:00.000000");
        return record;
    }
}
