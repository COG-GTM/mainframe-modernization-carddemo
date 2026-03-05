package com.carddemo.controller;

import com.carddemo.dto.ReportRequest;
import com.carddemo.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Report generation controller — replaces CORPT00C CICS transaction.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/transaction-report")
    public ResponseEntity<Map<String, Object>> submitReport(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.submitTransactionReport(request));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable String reportId) {
        // TODO: Implement report retrieval from storage
        return ResponseEntity.ok(Map.of(
            "reportId", reportId,
            "status", "PENDING",
            "message", "Report retrieval will be available after batch module integration"
        ));
    }
}
