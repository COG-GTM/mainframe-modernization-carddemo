package com.carddemo.online.report;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * COBOL program: CORPT00C — "Transaction report submission" (transaction CR00).
 *
 * <p>Files: TRANSACT (copybook CVTRA05Y) is read by the submitted batch job, not by CR00 itself.
 * Screen: BMS mapset CORPT00, map CORPT0A. Date validity is checked by CSUTLDTC on the mainframe.
 *
 * <p>The program builds the TRNRPT00 JCL in working storage and writes it to the JOBS transient
 * data queue; here the validated request is handed to a {@link TransactionReportJobSubmitter}. The
 * report type precedence (monthly, then yearly, then custom), the date range each type produces,
 * the validation order and every message text are those of the COBOL program.
 */
@Service
public class TransactionReportService {

    static final String MSG_SELECT_REPORT_TYPE = "Select a report type to print report...";
    static final String MSG_START_MONTH_EMPTY = "Start Date - Month can NOT be empty...";
    static final String MSG_START_DAY_EMPTY = "Start Date - Day can NOT be empty...";
    static final String MSG_START_YEAR_EMPTY = "Start Date - Year can NOT be empty...";
    static final String MSG_END_MONTH_EMPTY = "End Date - Month can NOT be empty...";
    static final String MSG_END_DAY_EMPTY = "End Date - Day can NOT be empty...";
    static final String MSG_END_YEAR_EMPTY = "End Date - Year can NOT be empty...";
    static final String MSG_START_MONTH_INVALID = "Start Date - Not a valid Month...";
    static final String MSG_START_DAY_INVALID = "Start Date - Not a valid Day...";
    static final String MSG_START_YEAR_INVALID = "Start Date - Not a valid Year...";
    static final String MSG_END_MONTH_INVALID = "End Date - Not a valid Month...";
    static final String MSG_END_DAY_INVALID = "End Date - Not a valid Day...";
    static final String MSG_END_YEAR_INVALID = "End Date - Not a valid Year...";
    static final String MSG_START_DATE_INVALID = "Start Date - Not a valid date...";
    static final String MSG_END_DATE_INVALID = "End Date - Not a valid date...";

    static final String REPORT_MONTHLY = "Monthly";
    static final String REPORT_YEARLY = "Yearly";
    static final String REPORT_CUSTOM = "Custom";

    private final TransactionReportJobSubmitter jobSubmitter;
    private final Clock clock;

    @Autowired
    public TransactionReportService(TransactionReportJobSubmitter jobSubmitter) {
        this(jobSubmitter, Clock.systemDefaultZone());
    }

    /** FUNCTION CURRENT-DATE is driven by an injectable clock so tests stay deterministic. */
    TransactionReportService(TransactionReportJobSubmitter jobSubmitter, Clock clock) {
        this.jobSubmitter = jobSubmitter;
        this.clock = clock;
    }

    /** PROCESS-ENTER-KEY. */
    public TransactionReportResponse submit(TransactionReportRequest request) {
        if (isSelected(request.getMonthly())) {
            LocalDate today = LocalDate.now(clock);
            return submitJobToInternalReader(
                    request,
                    REPORT_MONTHLY,
                    today.withDayOfMonth(1).toString(),
                    today.withDayOfMonth(today.lengthOfMonth()).toString());
        }

        if (isSelected(request.getYearly())) {
            int year = LocalDate.now(clock).getYear();
            return submitJobToInternalReader(
                    request, REPORT_YEARLY, year + "-01-01", year + "-12-31");
        }

        if (isSelected(request.getCustom())) {
            return submitCustom(request);
        }

        return failure(MSG_SELECT_REPORT_TYPE, null, null, null);
    }

    private TransactionReportResponse submitCustom(TransactionReportRequest request) {
        String startMonth = trimToEmpty(request.getStartMonth());
        String startDay = trimToEmpty(request.getStartDay());
        String startYear = trimToEmpty(request.getStartYear());
        String endMonth = trimToEmpty(request.getEndMonth());
        String endDay = trimToEmpty(request.getEndDay());
        String endYear = trimToEmpty(request.getEndYear());

        if (startMonth.isEmpty()) {
            return customFailure(MSG_START_MONTH_EMPTY);
        }
        if (startDay.isEmpty()) {
            return customFailure(MSG_START_DAY_EMPTY);
        }
        if (startYear.isEmpty()) {
            return customFailure(MSG_START_YEAR_EMPTY);
        }
        if (endMonth.isEmpty()) {
            return customFailure(MSG_END_MONTH_EMPTY);
        }
        if (endDay.isEmpty()) {
            return customFailure(MSG_END_DAY_EMPTY);
        }
        if (endYear.isEmpty()) {
            return customFailure(MSG_END_YEAR_EMPTY);
        }

        // FUNCTION NUMVAL-C followed by MOVE back into the PIC 9 screen fields: '5' becomes '05'.
        if (!isNumeric(startMonth) || numberOf(startMonth) > 12) {
            return customFailure(MSG_START_MONTH_INVALID);
        }
        if (!isNumeric(startDay) || numberOf(startDay) > 31) {
            return customFailure(MSG_START_DAY_INVALID);
        }
        if (!isNumeric(startYear)) {
            return customFailure(MSG_START_YEAR_INVALID);
        }
        if (!isNumeric(endMonth) || numberOf(endMonth) > 12) {
            return customFailure(MSG_END_MONTH_INVALID);
        }
        if (!isNumeric(endDay) || numberOf(endDay) > 31) {
            return customFailure(MSG_END_DAY_INVALID);
        }
        if (!isNumeric(endYear)) {
            return customFailure(MSG_END_YEAR_INVALID);
        }

        String startDate = pad(startYear, 4) + "-" + pad(startMonth, 2) + "-" + pad(startDay, 2);
        String endDate = pad(endYear, 4) + "-" + pad(endMonth, 2) + "-" + pad(endDay, 2);

        if (!isRealDate(startDate)) {
            return failure(MSG_START_DATE_INVALID, REPORT_CUSTOM, startDate, endDate);
        }
        if (!isRealDate(endDate)) {
            return failure(MSG_END_DATE_INVALID, REPORT_CUSTOM, startDate, endDate);
        }

        return submitJobToInternalReader(request, REPORT_CUSTOM, startDate, endDate);
    }

    /** SUBMIT-JOB-TO-INTRDR: the confirmation gate in front of the internal reader write. */
    private TransactionReportResponse submitJobToInternalReader(
            TransactionReportRequest request, String reportName, String startDate, String endDate) {

        String confirm = trimToEmpty(request.getConfirm());
        if (confirm.isEmpty()) {
            return failure(
                    "Please confirm to print the " + reportName + " report...",
                    reportName,
                    startDate,
                    endDate);
        }
        if (confirm.equalsIgnoreCase("N")) {
            // INITIALIZE-ALL-FIELDS then MOVE 'Y' TO WS-ERR-FLG: the screen is wiped, no message.
            return failure(null, reportName, startDate, endDate);
        }
        if (!confirm.equalsIgnoreCase("Y")) {
            return failure(
                    "\"" + confirm + "\" is not a valid value to confirm...",
                    reportName,
                    startDate,
                    endDate);
        }

        jobSubmitter.submit(reportName, startDate, endDate);

        return TransactionReportResponse.builder()
                .success(true)
                .message(reportName + " report submitted for printing ...")
                .reportName(reportName)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private TransactionReportResponse customFailure(String message) {
        return failure(message, REPORT_CUSTOM, null, null);
    }

    private TransactionReportResponse failure(
            String message, String reportName, String startDate, String endDate) {
        return TransactionReportResponse.builder()
                .success(false)
                .message(message)
                .reportName(reportName)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    /** CALL 'CSUTLDTC' with format YYYY-MM-DD: severity 0000 means the date exists. */
    private static boolean isRealDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isSelected(String field) {
        return !trimToEmpty(field).isEmpty();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static int numberOf(String value) {
        return Integer.parseInt(value);
    }

    private static String pad(String value, int length) {
        return value.length() >= length ? value : "0".repeat(length - value.length()) + value;
    }

    private static boolean isNumeric(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }
}
