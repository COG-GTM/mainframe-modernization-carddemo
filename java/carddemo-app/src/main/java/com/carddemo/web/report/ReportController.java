package com.carddemo.web.report;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.service.report.ReportException;
import com.carddemo.service.report.TransactionReportService;
import com.carddemo.web.report.dto.ReportErrorResponse;
import com.carddemo.web.report.dto.ReportRequest;
import com.carddemo.web.report.dto.ReportResponse;

/**
 * REST transaction-report request endpoint ported from {@code CORPT00C}.
 *
 * <p>{@code POST /api/reports/transactions} accepts a report type (MONTHLY / YEARLY / CUSTOM)
 * and, for custom, a date range. Submit without {@code confirm} to validate the request and be
 * prompted to confirm, then resubmit with {@code confirm=Y} to generate the report. Rejections
 * carry the verbatim {@code CORPT00C} messages.</p>
 */
@RestController
@RequestMapping("/api/reports/transactions")
public class ReportController {

    private final TransactionReportService reportService;

    public ReportController(TransactionReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ReportResponse requestReport(@RequestBody ReportRequest request) {
        return reportService.requestReport(request);
    }

    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ReportErrorResponse> handleReportException(ReportException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ReportErrorResponse(ex.getMessage()));
    }
}
