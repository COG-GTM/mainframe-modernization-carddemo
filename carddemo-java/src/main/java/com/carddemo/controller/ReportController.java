package com.carddemo.controller;

import com.carddemo.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReport() {
        return ResponseEntity.ok(reportService.generateReport());
    }
}
