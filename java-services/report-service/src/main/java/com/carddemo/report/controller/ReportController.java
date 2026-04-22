package com.carddemo.report.controller;

import com.carddemo.report.dto.ReportRequest;
import com.carddemo.report.dto.StatementDto;
import com.carddemo.report.dto.TransactionReportDto;
import com.carddemo.report.service.ReportService;
import com.carddemo.report.service.StatementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final StatementService statementService;

    public ReportController(ReportService reportService, StatementService statementService) {
        this.reportService = reportService;
        this.statementService = statementService;
    }

    /**
     * POST /api/reports/transactions
     * Generate a transaction detail report for a date range.
     * Mirrors CORPT00C's report submission and CBTRN03C's report generation.
     */
    @PostMapping("/transactions")
    public ResponseEntity<TransactionReportDto> generateTransactionReport(
            @RequestBody ReportRequest request) {
        TransactionReportDto report = reportService.generateTransactionReport(request);
        return ResponseEntity.ok(report);
    }

    /**
     * POST /api/reports/statements
     * Generate an account statement for an account/card.
     * Mirrors CBSTM03A's statement generation logic.
     */
    @PostMapping("/statements")
    public ResponseEntity<StatementDto> generateStatement(
            @RequestBody ReportRequest request) {
        StatementDto statement = statementService.generateStatement(request);
        return ResponseEntity.ok(statement);
    }

    /**
     * GET /api/reports/transactions/{reportId}
     * Retrieve a previously generated transaction report.
     */
    @GetMapping("/transactions/{reportId}")
    public ResponseEntity<TransactionReportDto> getTransactionReport(
            @PathVariable String reportId) {
        TransactionReportDto report = reportService.getReportById(reportId);
        return ResponseEntity.ok(report);
    }
}
