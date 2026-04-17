package com.carddemo.transaction.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for report request submission.
 *
 * COBOL Traceability: Replaces the confirmation message displayed by
 * CORPT00C after writing to the Transient Data queue.
 */
public record ReportResponse(
        Long reportId,
        LocalDate startDate,
        LocalDate endDate,
        String reportType,
        String status,
        LocalDateTime createdAt
) {
}
