package com.carddemo.batch.report;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** COBOL program: CBTRN03C — WS-DATEPARM-RECORD and the TRAN-PROC-TS range test. */
class ReportDateRangeTest {

    @Test
    void parsesTheDateParmRecord() {
        ReportDateRange range = ReportDateRange.parse("2022-01-01 2022-07-06" + " ".repeat(59));

        assertThat(range.startDate()).isEqualTo("2022-01-01");
        assertThat(range.endDate()).isEqualTo("2022-07-06");
    }

    @Test
    void comparesTheFirstTenBytesOfTheProcessingTimestampInclusively() {
        ReportDateRange range = new ReportDateRange("2022-01-01", "2022-07-06");

        assertThat(range.contains("2022-01-01-00.00.00.000000")).isTrue();
        assertThat(range.contains("2022-07-06-23.59.59.990000")).isTrue();
        assertThat(range.contains("2021-12-31-23.59.59.990000")).isFalse();
        assertThat(range.contains("2022-07-07-00.00.00.000000")).isFalse();
        assertThat(range.contains("")).isFalse();
    }
}
