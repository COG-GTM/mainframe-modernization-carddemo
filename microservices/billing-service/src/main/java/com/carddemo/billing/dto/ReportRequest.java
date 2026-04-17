package com.carddemo.billing.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for report submission.
 *
 * Derived from CORPT00C.cbl input fields:
 * - ACTIDINI (Account ID)
 * - WS-START-DATE (YYYY-MM-DD format)
 * - WS-END-DATE (YYYY-MM-DD format)
 *
 * The original COBOL program supported Monthly, Yearly, and Custom
 * date ranges. In the microservice, the caller computes the date
 * range and provides it directly.
 */
public record ReportRequest(
        @NotBlank(message = "Account ID is required")
        String accountId,

        @NotBlank(message = "Start date is required")
        String startDate,

        @NotBlank(message = "End date is required")
        String endDate
) {
}
