package com.carddemo.online.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.online.report.RecordingTransactionReportJobSubmitter.SubmittedReport;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** COBOL program: CORPT00C — report type, date range and confirmation handling. */
class TransactionReportServiceTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2024-02-15T10:00:00Z"), ZoneOffset.UTC);

    private RecordingTransactionReportJobSubmitter submitter;
    private TransactionReportService service;

    @BeforeEach
    void setUp() {
        submitter = new RecordingTransactionReportJobSubmitter();
        service = new TransactionReportService(submitter, CLOCK);
    }

    @Test
    void theMonthlyReportCoversTheCurrentMonth() {
        TransactionReportResponse response =
                service.submit(TransactionReportRequest.builder().monthly("Y").confirm("Y").build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getReportName()).isEqualTo("Monthly");
        assertThat(response.getStartDate()).isEqualTo("2024-02-01");
        assertThat(response.getEndDate()).isEqualTo("2024-02-29");
        assertThat(response.getMessage()).isEqualTo("Monthly report submitted for printing ...");
        assertThat(submitter.getSubmitted())
                .containsExactly(new SubmittedReport("Monthly", "2024-02-01", "2024-02-29"));
    }

    @Test
    void theYearlyReportCoversTheCurrentYear() {
        TransactionReportResponse response =
                service.submit(TransactionReportRequest.builder().yearly("Y").confirm("Y").build());

        assertThat(response.getStartDate()).isEqualTo("2024-01-01");
        assertThat(response.getEndDate()).isEqualTo("2024-12-31");
    }

    @Test
    void aCustomRangeIsZeroPaddedAndSubmitted() {
        TransactionReportResponse response = service.submit(TransactionReportRequest.builder()
                .custom("Y")
                .startMonth("1")
                .startDay("5")
                .startYear("2023")
                .endMonth("12")
                .endDay("31")
                .endYear("2023")
                .confirm("Y")
                .build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStartDate()).isEqualTo("2023-01-05");
        assertThat(response.getEndDate()).isEqualTo("2023-12-31");
    }

    @Test
    void aReportTypeIsRequired() {
        TransactionReportResponse response =
                service.submit(TransactionReportRequest.builder().confirm("Y").build());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(TransactionReportService.MSG_SELECT_REPORT_TYPE);
        assertThat(submitter.getSubmitted()).isEmpty();
    }

    @Test
    void confirmationIsRequiredBeforeSubmission() {
        TransactionReportResponse prompt =
                service.submit(TransactionReportRequest.builder().monthly("Y").build());
        assertThat(prompt.isSuccess()).isFalse();
        assertThat(prompt.getMessage()).isEqualTo("Please confirm to print the Monthly report...");

        TransactionReportResponse declined =
                service.submit(TransactionReportRequest.builder().monthly("Y").confirm("N").build());
        assertThat(declined.getMessage()).isNull();

        TransactionReportResponse invalid =
                service.submit(TransactionReportRequest.builder().monthly("Y").confirm("X").build());
        assertThat(invalid.getMessage()).isEqualTo("\"X\" is not a valid value to confirm...");

        assertThat(submitter.getSubmitted()).isEmpty();
    }

    @Test
    void customDateValidationsMatchTheCobol() {
        assertThat(message(custom().startMonth("").build()))
                .isEqualTo(TransactionReportService.MSG_START_MONTH_EMPTY);
        assertThat(message(custom().startDay("").build()))
                .isEqualTo(TransactionReportService.MSG_START_DAY_EMPTY);
        assertThat(message(custom().startYear("").build()))
                .isEqualTo(TransactionReportService.MSG_START_YEAR_EMPTY);
        assertThat(message(custom().endMonth("").build()))
                .isEqualTo(TransactionReportService.MSG_END_MONTH_EMPTY);
        assertThat(message(custom().endDay("").build()))
                .isEqualTo(TransactionReportService.MSG_END_DAY_EMPTY);
        assertThat(message(custom().endYear("").build()))
                .isEqualTo(TransactionReportService.MSG_END_YEAR_EMPTY);
        assertThat(message(custom().startMonth("13").build()))
                .isEqualTo(TransactionReportService.MSG_START_MONTH_INVALID);
        assertThat(message(custom().startDay("32").build()))
                .isEqualTo(TransactionReportService.MSG_START_DAY_INVALID);
        assertThat(message(custom().startYear("2O23").build()))
                .isEqualTo(TransactionReportService.MSG_START_YEAR_INVALID);
        assertThat(message(custom().endMonth("AB").build()))
                .isEqualTo(TransactionReportService.MSG_END_MONTH_INVALID);
        assertThat(message(custom().endDay("99").build()))
                .isEqualTo(TransactionReportService.MSG_END_DAY_INVALID);
        assertThat(message(custom().endYear("").build()))
                .isEqualTo(TransactionReportService.MSG_END_YEAR_EMPTY);
        assertThat(message(custom().startMonth("02").startDay("30").build()))
                .isEqualTo(TransactionReportService.MSG_START_DATE_INVALID);
        assertThat(message(custom().endMonth("04").endDay("31").build()))
                .isEqualTo(TransactionReportService.MSG_END_DATE_INVALID);
        assertThat(submitter.getSubmitted()).isEmpty();
    }

    private TransactionReportRequest.TransactionReportRequestBuilder custom() {
        return TransactionReportRequest.builder()
                .custom("Y")
                .startMonth("01")
                .startDay("05")
                .startYear("2023")
                .endMonth("12")
                .endDay("31")
                .endYear("2023")
                .confirm("Y");
    }

    private String message(TransactionReportRequest request) {
        return service.submit(request).getMessage();
    }
}
