package com.carddemo.report.service;

import com.carddemo.report.dto.ReportRequest;
import com.carddemo.report.dto.TransactionReportDto;
import com.carddemo.report.exception.ResourceNotFoundException;
import com.carddemo.report.model.Transaction;
import com.carddemo.report.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private Transaction txn1;
    private Transaction txn2;
    private Transaction txn3;

    @BeforeEach
    void setUp() {
        txn1 = new Transaction();
        txn1.setTranId("0000000000000001");
        txn1.setTranCardNum("4111111111111111");
        txn1.setTranDesc("Purchase at Amazon");
        txn1.setTranAmt(new BigDecimal("125.50"));
        txn1.setTranMerchantName("Amazon.com");
        txn1.setTranMerchantCity("Seattle");
        txn1.setTranTypeCd("SA");
        txn1.setTranSource("ONLINE");
        txn1.setTranProcTs(LocalDateTime.of(2024, 1, 10, 10, 30));
        txn1.setAccountId("00000000001");

        txn2 = new Transaction();
        txn2.setTranId("0000000000000002");
        txn2.setTranCardNum("4111111111111111");
        txn2.setTranDesc("Grocery shopping");
        txn2.setTranAmt(new BigDecimal("89.75"));
        txn2.setTranMerchantName("Whole Foods");
        txn2.setTranMerchantCity("Springfield");
        txn2.setTranTypeCd("SA");
        txn2.setTranSource("POS");
        txn2.setTranProcTs(LocalDateTime.of(2024, 1, 12, 14, 15));
        txn2.setAccountId("00000000001");

        txn3 = new Transaction();
        txn3.setTranId("0000000000000003");
        txn3.setTranCardNum("4222222222222222");
        txn3.setTranDesc("Netflix subscription");
        txn3.setTranAmt(new BigDecimal("15.99"));
        txn3.setTranMerchantName("Netflix");
        txn3.setTranMerchantCity("Los Gatos");
        txn3.setTranTypeCd("SA");
        txn3.setTranSource("ONLINE");
        txn3.setTranProcTs(LocalDateTime.of(2024, 1, 5, 0, 1));
        txn3.setAccountId("00000000002");
    }

    @Test
    void generateTransactionReport_groupsByCardAndCalculatesTotals() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-01-01");
        request.setEndDate("2024-01-31");

        List<Transaction> transactions = Arrays.asList(txn1, txn2, txn3);
        when(transactionRepository.findByTranProcTsBetweenOrderByTranCardNumAscTranProcTsAsc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        TransactionReportDto report = reportService.generateTransactionReport(request);

        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertEquals("2024-01-01", report.getStartDate());
        assertEquals("2024-01-31", report.getEndDate());
        assertEquals(2, report.getCardGroups().size());

        // First card group: 4111111111111111 with txn1 + txn2
        TransactionReportDto.CardGroup group1 = report.getCardGroups().get(0);
        assertEquals("4111111111111111", group1.getCardNumber());
        assertEquals(2, group1.getTransactions().size());
        assertEquals(new BigDecimal("215.25"), group1.getSubtotal());

        // Second card group: 4222222222222222 with txn3
        TransactionReportDto.CardGroup group2 = report.getCardGroups().get(1);
        assertEquals("4222222222222222", group2.getCardNumber());
        assertEquals(1, group2.getTransactions().size());
        assertEquals(new BigDecimal("15.99"), group2.getSubtotal());

        // Grand total = 215.25 + 15.99 = 231.24
        assertEquals(new BigDecimal("231.24"), report.getGrandTotal());
    }

    @Test
    void generateTransactionReport_emptyResult() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-06-01");
        request.setEndDate("2024-06-30");

        when(transactionRepository.findByTranProcTsBetweenOrderByTranCardNumAscTranProcTsAsc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        TransactionReportDto report = reportService.generateTransactionReport(request);

        assertNotNull(report);
        assertTrue(report.getCardGroups().isEmpty());
        assertEquals(BigDecimal.ZERO, report.getGrandTotal());
    }

    @Test
    void generateTransactionReport_invalidDateRange() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-01-31");
        request.setEndDate("2024-01-01");

        assertThrows(IllegalArgumentException.class,
                () -> reportService.generateTransactionReport(request));
    }

    @Test
    void generateTransactionReport_missingStartDate() {
        ReportRequest request = new ReportRequest();
        request.setEndDate("2024-01-31");

        assertThrows(IllegalArgumentException.class,
                () -> reportService.generateTransactionReport(request));
    }

    @Test
    void generateTransactionReport_missingEndDate() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-01-01");

        assertThrows(IllegalArgumentException.class,
                () -> reportService.generateTransactionReport(request));
    }

    @Test
    void getReportById_existingReport() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-01-01");
        request.setEndDate("2024-01-31");

        when(transactionRepository.findByTranProcTsBetweenOrderByTranCardNumAscTranProcTsAsc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(txn1));

        TransactionReportDto generated = reportService.generateTransactionReport(request);
        TransactionReportDto retrieved = reportService.getReportById(generated.getReportId());

        assertEquals(generated.getReportId(), retrieved.getReportId());
    }

    @Test
    void getReportById_nonExistentReport() {
        assertThrows(ResourceNotFoundException.class,
                () -> reportService.getReportById("nonexistent-id"));
    }

    @Test
    void generateTransactionReport_reportLineFields() {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-01-01");
        request.setEndDate("2024-01-31");

        when(transactionRepository.findByTranProcTsBetweenOrderByTranCardNumAscTranProcTsAsc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(txn1));

        TransactionReportDto report = reportService.generateTransactionReport(request);
        TransactionReportDto.ReportLine line = report.getCardGroups().get(0).getTransactions().get(0);

        assertEquals("0000000000000001", line.getTransactionId());
        assertEquals("2024-01-10", line.getDate());
        assertEquals("Purchase at Amazon", line.getDescription());
        assertEquals("Amazon.com", line.getMerchantName());
        assertEquals("Seattle", line.getMerchantCity());
        assertEquals(new BigDecimal("125.50"), line.getAmount());
        assertEquals("SA", line.getTypeCd());
        assertEquals("ONLINE", line.getSource());
    }
}
