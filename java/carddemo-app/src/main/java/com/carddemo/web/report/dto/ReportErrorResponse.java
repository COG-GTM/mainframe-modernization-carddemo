package com.carddemo.web.report.dto;

/**
 * Error body for a rejected {@code POST /api/reports/transactions} request, carrying the
 * verbatim {@code CORPT00C} {@code ERRMSGO} message.
 *
 * @param message the operator message that would have been shown on {@code CORPT0A}
 */
public record ReportErrorResponse(String message) {
}
