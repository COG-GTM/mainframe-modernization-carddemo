package com.carddemo.reports.controller;

import com.carddemo.reports.dto.ErrorResponse;
import com.carddemo.reports.dto.ReportRequest;
import com.carddemo.reports.dto.ReportResponse;
import com.carddemo.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Transaction report generation and retrieval")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Submit a report request",
            description = "Submit a transaction report request with type and date range. "
                    + "Report types: 01 (Monthly), 02 (Yearly), 03 (Custom). "
                    + "Report generation is asynchronous.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Report submitted successfully",
                    content = @Content(schema = @Schema(implementation = ReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReportResponse> submitReport(
            @Valid @RequestBody ReportRequest request) {
        ReportResponse response = reportService.submitReport(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Check report status",
            description = "Retrieve the current status of a submitted report job.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report status retrieved",
                    content = @Content(schema = @Schema(implementation = ReportResponse.class))),
            @ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReportResponse> getReportStatus(@PathVariable String id) {
        ReportResponse response = reportService.getReportStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download completed report",
            description = "Download the generated report file (CSV format).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report file"),
            @ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Report not ready for download",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Resource> downloadReport(@PathVariable String id)
            throws MalformedURLException {
        Path filePath = reportService.getReportFilePath(id);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String fileName = filePath.getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
