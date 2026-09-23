package com.carddemo.web;

import com.carddemo.service.TransactionReportService;
import com.carddemo.web.dto.JobExecutionResponse;
import com.carddemo.web.dto.ReportRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CORPT00C - transaction reports (the screen that submitted the TRANREPT job). */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final TransactionReportService reportService;

    public ReportController(TransactionReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/transactions")
    public JobExecutionResponse generate(@RequestBody ReportRequest request) {
        var path = reportService.generate(request.reportType(), request.startDate(), request.endDate());
        return new JobExecutionResponse("TRANREPT", null, "COMPLETED", path.toString());
    }
}
