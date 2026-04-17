package com.carddemo.shared.util;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of a date validation operation, modeled after CSUTLDTC.cbl output.
 *
 * The original COBOL program returns:
 * - Severity code (0 = success, non-zero = error)
 * - Message number identifying the specific error
 * - Result text ("Date is valid", "Invalid month", etc.)
 * - The tested date string
 * - The format mask used
 */
public class DateValidationResult {

    @JsonProperty("valid")
    private final boolean valid;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("severityCode")
    private final int severityCode;

    @JsonProperty("testedDate")
    private final String testedDate;

    @JsonProperty("formatUsed")
    private final String formatUsed;

    public DateValidationResult(boolean valid, String message, int severityCode,
                                String testedDate, String formatUsed) {
        this.valid = valid;
        this.message = message;
        this.severityCode = severityCode;
        this.testedDate = testedDate;
        this.formatUsed = formatUsed;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public int getSeverityCode() {
        return severityCode;
    }

    public String getTestedDate() {
        return testedDate;
    }

    public String getFormatUsed() {
        return formatUsed;
    }

    @Override
    public String toString() {
        return String.format("DateValidationResult{valid=%s, message='%s', severityCode=%d, " +
                "testedDate='%s', formatUsed='%s'}", valid, message, severityCode, testedDate, formatUsed);
    }
}
