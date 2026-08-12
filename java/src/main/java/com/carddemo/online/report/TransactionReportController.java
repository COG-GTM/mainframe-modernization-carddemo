package com.carddemo.online.report;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: CORPT00C (transaction CR00) — transaction report submission, BMS map CORPT0A.
 *
 * <p>The response is the accepted report request: report name and the resolved date range that the
 * mainframe would have passed to the TRANREPT job as PARM-START-DATE / PARM-END-DATE.
 */
@RestController
@RequestMapping("/api/reports/transactions")
public class TransactionReportController {

    private final TransactionReportService reportService;

    public TransactionReportController(TransactionReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<TransactionReportResponse> submit(@RequestBody TransactionReportRequest request) {
        TransactionReportResponse response = reportService.submit(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
