package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.ReportRequest;
import com.carddemo.transaction.dto.ReportResponse;
import com.carddemo.transaction.entity.ReportRequestEntity;
import com.carddemo.transaction.exception.InvalidRequestException;
import com.carddemo.transaction.repository.ReportRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for transaction report request management.
 *
 * COBOL Traceability: Replaces CORPT00C.cbl (Txn CR00).
 * The COBOL program:
 * 1. Accepts date range input from the user
 * 2. Validates dates via CALL to CSUTLDTC utility
 * 3. Writes report request to CICS Transient Data queue (WRITEQ TD)
 * 4. The TD queue triggers batch report generation (CBTRN03C)
 *
 * In the modern implementation, report requests are persisted to a database
 * table and processed asynchronously by the TransactionReportJob batch job.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportRequestRepository reportRequestRepository;

    public ReportService(ReportRequestRepository reportRequestRepository) {
        this.reportRequestRepository = reportRequestRepository;
    }

    /**
     * Submit a transaction report request.
     *
     * COBOL Traceability: Replaces CORPT00C PROCESS-ENTER-KEY paragraph
     * which validates date range and writes to TD queue.
     * Date validation replaces the CALL 'CSUTLDTC' USING CSUTLDTC-PARM pattern.
     */
    @Transactional
    public ReportResponse submitReportRequest(ReportRequest request) {
        // Validate date range (replaces CALL to CSUTLDTC date utility)
        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidRequestException(
                    "End date must be on or after start date");
        }

        // Validate report type
        String reportType = request.reportType().toUpperCase();
        if (!reportType.equals("MONTHLY") && !reportType.equals("YEARLY")
                && !reportType.equals("CUSTOM")) {
            throw new InvalidRequestException(
                    "Invalid report type. Valid values: MONTHLY, YEARLY, CUSTOM");
        }

        // Create report request (replaces WRITEQ TD to transient data queue)
        ReportRequestEntity entity = new ReportRequestEntity();
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setReportType(reportType);
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());

        ReportRequestEntity saved = reportRequestRepository.save(entity);
        log.info("Report request submitted: id={}, type={}, dateRange={} to {}",
                saved.getId(), reportType, request.startDate(), request.endDate());

        return new ReportResponse(
                saved.getId(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getReportType(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }
}
