package com.carddemo.web.report;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.carddemo.service.report.ReportException;
import com.carddemo.service.report.TransactionReportService;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.NavigationService;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.report.dto.ReportRequest;
import com.carddemo.web.report.dto.ReportResponse;
import com.carddemo.web.report.dto.ReportType;

/**
 * {@link ScreenHandler} for the transaction-report request screen ({@code CORPT00} /
 * {@code CORPT00C}), registered against {@link CardDemoProgram#REPORTS}. Bridges a
 * pseudo-conversational navigation turn to {@link TransactionReportService}: the {@code CORPT0A}
 * input fields arrive as {@link ScreenRequest} fields, and the outcome is returned as a
 * {@link ScreenResult} that keeps control on the screen ({@code RETURN TRANSID('CR00')}),
 * matching the COBOL which always re-displays {@code CORPT0A}.
 */
@Component
public class ReportScreenHandler implements ScreenHandler {

    /** Report-type selector ({@code MONTHLY}/{@code YEARLY}/{@code CUSTOM} radio flags). */
    public static final String FIELD_REPORT_TYPE = "reportType";
    public static final String FIELD_START_MONTH = "startMonth";
    public static final String FIELD_START_DAY = "startDay";
    public static final String FIELD_START_YEAR = "startYear";
    public static final String FIELD_END_MONTH = "endMonth";
    public static final String FIELD_END_DAY = "endDay";
    public static final String FIELD_END_YEAR = "endYear";
    /** {@code CONFIRM} — the {@code (Y/N)} confirmation field. */
    public static final String FIELD_CONFIRM = "confirm";

    private final TransactionReportService reportService;
    private final NavigationService navigation;

    public ReportScreenHandler(TransactionReportService reportService,
            NavigationService navigation) {
        this.reportService = reportService;
        this.navigation = navigation;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.REPORTS;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        // First entry (ENTER): display the empty screen; process input on RE-ENTRY only.
        if (navigation.beginTurn(commarea)) {
            return ScreenResult.stay();
        }
        ReportRequest reportRequest = new ReportRequest(
                parseType(request.field(FIELD_REPORT_TYPE)),
                request.field(FIELD_START_MONTH),
                request.field(FIELD_START_DAY),
                request.field(FIELD_START_YEAR),
                request.field(FIELD_END_MONTH),
                request.field(FIELD_END_DAY),
                request.field(FIELD_END_YEAR),
                request.field(FIELD_CONFIRM));
        try {
            ReportResponse response = reportService.requestReport(reportRequest);
            return ScreenResult.stay(response.message(), response);
        } catch (ReportException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }

    private static ReportType parseType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ReportType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
