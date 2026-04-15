package com.carddemo.report.service;

import com.carddemo.report.dto.ReportRequest;
import com.carddemo.report.dto.TransactionReportDto;
import com.carddemo.report.dto.TransactionReportDto.CardGroup;
import com.carddemo.report.dto.TransactionReportDto.ReportLine;
import com.carddemo.report.exception.ResourceNotFoundException;
import com.carddemo.report.model.Transaction;
import com.carddemo.report.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for generating transaction detail reports.
 * Implements the business logic from CBTRN03C.cbl:
 * - Reads transactions within a date range (TRAN-PROC-TS between start/end)
 * - Groups by card number (WS-CURR-CARD-NUM tracking)
 * - Calculates subtotals per card (WS-ACCOUNT-TOTAL) and grand total (WS-GRAND-TOTAL)
 * - Formats detail lines with transaction ID, description, merchant, amount
 */
@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final Map<String, TransactionReportDto> reportStore = new ConcurrentHashMap<>();

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generate a transaction detail report for the given date range.
     * Mirrors CBTRN03C logic: read date parms, iterate transactions,
     * group by card number, write subtotals and grand total.
     */
    public TransactionReportDto generateTransactionReport(ReportRequest request) {
        validateDateRange(request);

        LocalDate startLocalDate = LocalDate.parse(request.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endLocalDate = LocalDate.parse(request.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime startDateTime = startLocalDate.atStartOfDay();
        LocalDateTime endDateTime = endLocalDate.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository
                .findByTranProcTsBetweenOrderByTranCardNumAscTranProcTsAsc(startDateTime, endDateTime);

        TransactionReportDto report = new TransactionReportDto();
        report.setReportId(UUID.randomUUID().toString());
        report.setReportName("Transaction Detail Report");
        report.setStartDate(request.getStartDate());
        report.setEndDate(request.getEndDate());
        report.setGeneratedAt(LocalDateTime.now());

        // Group transactions by card number, analogous to CBTRN03C's
        // WS-CURR-CARD-NUM tracking and 1120-WRITE-ACCOUNT-TOTALS
        Map<String, List<Transaction>> groupedByCard = new LinkedHashMap<>();
        for (Transaction txn : transactions) {
            String cardNum = txn.getTranCardNum() != null ? txn.getTranCardNum() : "UNKNOWN";
            groupedByCard.computeIfAbsent(cardNum, k -> new ArrayList<>()).add(txn);
        }

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map.Entry<String, List<Transaction>> entry : groupedByCard.entrySet()) {
            CardGroup cardGroup = new CardGroup();
            cardGroup.setCardNumber(entry.getKey());

            BigDecimal subtotal = BigDecimal.ZERO;
            List<ReportLine> lines = new ArrayList<>();

            for (Transaction txn : entry.getValue()) {
                ReportLine line = new ReportLine();
                line.setTransactionId(txn.getTranId());
                line.setDate(txn.getTranProcTs() != null
                        ? txn.getTranProcTs().toLocalDate().toString() : "");
                line.setDescription(txn.getTranDesc());
                line.setMerchantName(txn.getTranMerchantName());
                line.setMerchantCity(txn.getTranMerchantCity());
                line.setAmount(txn.getTranAmt());
                line.setTypeCd(txn.getTranTypeCd());
                line.setSource(txn.getTranSource());

                if (txn.getTranAmt() != null) {
                    subtotal = subtotal.add(txn.getTranAmt());
                }

                lines.add(line);
            }

            cardGroup.setTransactions(lines);
            cardGroup.setSubtotal(subtotal);
            if (!entry.getValue().isEmpty()) {
                cardGroup.setAccountId(entry.getValue().get(0).getAccountId());
            }

            grandTotal = grandTotal.add(subtotal);
            report.getCardGroups().add(cardGroup);
        }

        report.setGrandTotal(grandTotal);

        // Store the report for later retrieval (GET /api/reports/transactions/{reportId})
        reportStore.put(report.getReportId(), report);

        return report;
    }

    /**
     * Retrieve a previously generated report by its ID.
     */
    public TransactionReportDto getReportById(String reportId) {
        TransactionReportDto report = reportStore.get(reportId);
        if (report == null) {
            throw new ResourceNotFoundException("Report not found with id: " + reportId);
        }
        return report;
    }

    private void validateDateRange(ReportRequest request) {
        if (request.getStartDate() == null || request.getStartDate().isBlank()) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (request.getEndDate() == null || request.getEndDate().isBlank()) {
            throw new IllegalArgumentException("End date is required");
        }
        LocalDate start = LocalDate.parse(request.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(request.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
    }
}
