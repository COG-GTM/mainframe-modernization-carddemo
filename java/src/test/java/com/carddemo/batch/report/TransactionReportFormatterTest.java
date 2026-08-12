package com.carddemo.batch.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** COBOL copybook: CVTRA07Y — edited picture and record layout of the CBTRN03C report. */
class TransactionReportFormatterTest {

    private final TransactionReportFormatter formatter = new TransactionReportFormatter();

    @Test
    void editsTheDetailAmountWithZeroSuppressionAndAFixedSign() {
        assertThat(formatter.editedAmount(new BigDecimal("1234.56"), '-')).isEqualTo("       1,234.56");
        assertThat(formatter.editedAmount(new BigDecimal("-1234.56"), '-')).isEqualTo("-      1,234.56");
        assertThat(formatter.editedAmount(new BigDecimal("0.56"), '-')).isEqualTo("            .56");
        assertThat(formatter.editedAmount(new BigDecimal("123456789.01"), '-'))
                .isEqualTo(" 123,456,789.01");
        assertThat(formatter.editedAmount(new BigDecimal("1234.56"), '-')).hasSize(15);
    }

    @Test
    void blanksTheWholeAmountFieldWhenTheValueIsZero() {
        assertThat(formatter.editedAmount(BigDecimal.ZERO, '-')).isEqualTo(" ".repeat(15));
        assertThat(formatter.editedAmount(BigDecimal.ZERO, '+')).isEqualTo(" ".repeat(15));
        assertThat(formatter.editedAmount(null, '+')).isEqualTo(" ".repeat(15));
    }

    @Test
    void printsAnExplicitPlusOnTheTotalLines() {
        assertThat(formatter.editedAmount(new BigDecimal("2500.00"), '+')).isEqualTo("+      2,500.00");
        assertThat(formatter.editedAmount(new BigDecimal("-2500.00"), '+')).isEqualTo("-      2,500.00");
    }

    @Test
    void writesEveryLineAtTheRecordLengthOfTheReportFile() {
        ReportDateRange range = new ReportDateRange("2022-01-01", "2022-07-06");

        assertThat(formatter.nameHeader(range)).hasSize(133);
        assertThat(formatter.columnHeader()).hasSize(133);
        assertThat(formatter.separator()).isEqualTo("-".repeat(133));
        assertThat(formatter.blankLine()).hasSize(133).isBlank();
        assertThat(formatter.pageTotals(new BigDecimal("10.00"))).hasSize(133);
        assertThat(formatter.accountTotals(new BigDecimal("10.00"))).hasSize(133);
        assertThat(formatter.grandTotals(new BigDecimal("10.00"))).hasSize(133);
    }

    @Test
    void rendersTheReportNameHeaderWithTheDateRange() {
        String header = formatter.nameHeader(new ReportDateRange("2022-01-01", "2022-07-06"));

        assertThat(header).startsWith("DALYREPT");
        assertThat(header.substring(38, 79).trim()).isEqualTo("Daily Transaction Report");
        assertThat(header.substring(91, 115)).isEqualTo("2022-01-01 to 2022-07-06");
    }

    @Test
    void rendersTheDetailLineFieldsAtTheirCopybookOffsets() {
        String detail = formatter.detail("2022071800000001", 11L, "01", "Debit",
                5, "Interest", "System", new BigDecimal("10.00"));

        assertThat(detail.substring(0, 16)).isEqualTo("2022071800000001");
        assertThat(detail.substring(17, 28)).isEqualTo("00000000011");
        assertThat(detail.substring(29, 31)).isEqualTo("01");
        assertThat(detail.charAt(31)).isEqualTo('-');
        assertThat(detail.substring(32, 47)).isEqualTo("Debit          ");
        assertThat(detail.substring(48, 52)).isEqualTo("0005");
        assertThat(detail.charAt(52)).isEqualTo('-');
        assertThat(detail.substring(53, 82).trim()).isEqualTo("Interest");
        assertThat(detail.substring(83, 93)).isEqualTo("System    ");
        assertThat(detail.substring(97, 112)).isEqualTo("          10.00");
        assertThat(detail).hasSize(133);
    }

    @Test
    void rendersTheTotalLinesWithTheirDotLeaders() {
        assertThat(formatter.pageTotals(new BigDecimal("2500.00")).stripTrailing())
                .isEqualTo("Page Total " + ".".repeat(86) + "+      2,500.00");
        assertThat(formatter.accountTotals(new BigDecimal("2500.00")).stripTrailing())
                .isEqualTo("Account Total" + ".".repeat(84) + "+      2,500.00");
        assertThat(formatter.grandTotals(new BigDecimal("2500.00")).stripTrailing())
                .isEqualTo("Grand Total" + ".".repeat(86) + "+      2,500.00");
    }
}
