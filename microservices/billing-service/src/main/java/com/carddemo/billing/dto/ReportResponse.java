package com.carddemo.billing.dto;

import java.time.Instant;

/**
 * Response DTO for report submission.
 *
 * Modernized from CORPT00C.cbl's confirmation message:
 * "Monthly/Yearly/Custom report submitted for printing..."
 *
 * Instead of writing JCL to a TDQ, we publish a message to RabbitMQ
 * and return a confirmation with the request details.
 */
public record ReportResponse(
        String status,
        String accountId,
        String startDate,
        String endDate,
        Instant requestedAt
) {
}
