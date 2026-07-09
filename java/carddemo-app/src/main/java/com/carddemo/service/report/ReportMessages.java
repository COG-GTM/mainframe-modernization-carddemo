package com.carddemo.service.report;

import com.carddemo.web.report.dto.ReportType;

/**
 * Verbatim operator messages from {@code CORPT00C} ({@code ERRMSGO} of map {@code CORPT0A}),
 * reused by the REST report-request flow so the migrated logic returns the exact wording a
 * 3270 operator saw.
 */
public final class ReportMessages {

    /** {@code EVALUATE TRUE ... WHEN OTHER} — no report type flagged. */
    public static final String SELECT_REPORT_TYPE = "Select a report type to print report...";

    public static final String START_MONTH_EMPTY = "Start Date - Month can NOT be empty...";
    public static final String START_DAY_EMPTY = "Start Date - Day can NOT be empty...";
    public static final String START_YEAR_EMPTY = "Start Date - Year can NOT be empty...";
    public static final String END_MONTH_EMPTY = "End Date - Month can NOT be empty...";
    public static final String END_DAY_EMPTY = "End Date - Day can NOT be empty...";
    public static final String END_YEAR_EMPTY = "End Date - Year can NOT be empty...";

    public static final String START_MONTH_INVALID = "Start Date - Not a valid Month...";
    public static final String START_DAY_INVALID = "Start Date - Not a valid Day...";
    public static final String START_YEAR_INVALID = "Start Date - Not a valid Year...";
    public static final String END_MONTH_INVALID = "End Date - Not a valid Month...";
    public static final String END_DAY_INVALID = "End Date - Not a valid Day...";
    public static final String END_YEAR_INVALID = "End Date - Not a valid Year...";

    /** {@code CSUTLDTC} start-date validation failure. */
    public static final String START_DATE_INVALID = "Start Date - Not a valid date...";
    /** {@code CSUTLDTC} end-date validation failure. */
    public static final String END_DATE_INVALID = "End Date - Not a valid date...";

    private ReportMessages() {
    }

    /** {@code SUBMIT-JOB-TO-INTRDR}: {@code CONFIRMI = SPACES} prompt. */
    public static String confirmToPrint(ReportType type) {
        return "Please confirm to print the " + type.reportName() + " report...";
    }

    /** {@code SUBMIT-JOB-TO-INTRDR} {@code WHEN OTHER}: an invalid confirmation value. */
    public static String invalidConfirm(String value) {
        return "\"" + value + "\" is not a valid value to confirm...";
    }

    /** Success message: {@code STRING WS-REPORT-NAME ' report submitted for printing ...'}. */
    public static String submittedForPrinting(ReportType type) {
        return type.reportName() + " report submitted for printing ...";
    }
}
