package com.carddemo.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for transaction report generation.
 *
 * COBOL Traceability: Maps the input fields from CORPT00C (Txn CR00).
 * The COBOL program validates dates via CALL to CSUTLDTC utility and
 * writes report parameters to a CICS Transient Data queue.
 */
public record ReportRequest(
        @NotNull(message = "Start date is required")
        LocalDate startDate,
        @NotNull(message = "End date is required")
        LocalDate endDate,
        @NotBlank(message = "Report type is required")
        String reportType
) {
}
