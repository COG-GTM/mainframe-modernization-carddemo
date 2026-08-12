package com.carddemo.batch.report;

import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * COBOL copybook: CVTRA07Y — report record layouts of CBTRN03C (REPORT-NAME-HEADER,
 * TRANSACTION-HEADER-1/2, TRANSACTION-DETAIL-REPORT, REPORT-PAGE/ACCOUNT/GRAND-TOTALS).
 *
 * <p>Every line is returned padded to the 133 bytes of FD-REPTFILE-REC.
 */
@Service
public class TransactionReportFormatter {

    /** FD-REPTFILE-REC PIC X(133). */
    public static final int RECORD_LENGTH = 133;

    /** TRANSACTION-HEADER-2 PIC X(133) VALUE ALL '-'. */
    public static final String HEADER_SEPARATOR = "-".repeat(RECORD_LENGTH);

    /** WS-BLANK-LINE PIC X(133) VALUE SPACES. */
    public static final String BLANK_LINE = " ".repeat(RECORD_LENGTH);

    /** REPORT-NAME-HEADER with REPT-START-DATE / REPT-END-DATE filled in. */
    public String nameHeader(ReportDateRange range) {
        return pad(CobolUtils.padRight("DALYREPT", 38)
                + CobolUtils.padRight("Daily Transaction Report", 41)
                + CobolUtils.padRight("Date Range: ", 12)
                + CobolUtils.padRight(range.startDate(), 10)
                + " to "
                + CobolUtils.padRight(range.endDate(), 10));
    }

    /** TRANSACTION-HEADER-1 column captions. */
    public String columnHeader() {
        return pad(CobolUtils.padRight("Transaction ID", 17)
                + CobolUtils.padRight("Account ID", 12)
                + CobolUtils.padRight("Transaction Type", 19)
                + CobolUtils.padRight("Tran Category", 35)
                + CobolUtils.padRight("Tran Source", 14)
                + " "
                + CobolUtils.padRight("        Amount", 16));
    }

    /** TRANSACTION-HEADER-2, the dashed rule. */
    public String separator() {
        return HEADER_SEPARATOR;
    }

    /** WS-BLANK-LINE. */
    public String blankLine() {
        return BLANK_LINE;
    }

    /** TRANSACTION-DETAIL-REPORT as moved by 1120-WRITE-DETAIL. */
    public String detail(String transactionId,
                         Long accountId,
                         String typeCode,
                         String typeDescription,
                         Integer categoryCode,
                         String categoryDescription,
                         String source,
                         BigDecimal amount) {
        return pad(CobolUtils.padRight(transactionId, 16)
                + " "
                + CobolUtils.padLeftZeros(accountId, 11)
                + " "
                + CobolUtils.padRight(typeCode, 2)
                + "-"
                + CobolUtils.padRight(typeDescription, 15)
                + " "
                + CobolUtils.padLeftZeros(categoryCode, 4)
                + "-"
                + CobolUtils.padRight(categoryDescription, 29)
                + " "
                + CobolUtils.padRight(source, 10)
                + "    "
                + editedAmount(amount, '-')
                + "  ");
    }

    /** REPORT-PAGE-TOTALS. */
    public String pageTotals(BigDecimal pageTotal) {
        return totals("Page Total", 11, 86, pageTotal);
    }

    /** REPORT-ACCOUNT-TOTALS. */
    public String accountTotals(BigDecimal accountTotal) {
        return totals("Account Total", 13, 84, accountTotal);
    }

    /** REPORT-GRAND-TOTALS. */
    public String grandTotals(BigDecimal grandTotal) {
        return totals("Grand Total", 11, 86, grandTotal);
    }

    private String totals(String caption, int captionLength, int dots, BigDecimal total) {
        return pad(CobolUtils.padRight(caption, captionLength)
                + ".".repeat(dots)
                + editedAmount(total, '+'));
    }

    /**
     * Numeric editing of {@code PIC -ZZZ,ZZZ,ZZZ.ZZ} (detail amount) and
     * {@code PIC +ZZZ,ZZZ,ZZZ.ZZ} (totals): fifteen bytes, fixed sign in the first byte,
     * zero suppression of the nine integer digits (and of the group separators inside the
     * suppressed region). A zero value blanks the whole field, since every digit position is
     * a suppression symbol.
     */
    public String editedAmount(BigDecimal value, char signSymbol) {
        BigDecimal amount = CobolUtils.nvl(value).setScale(2, RoundingMode.DOWN);
        if (amount.signum() == 0) {
            return " ".repeat(15);
        }
        String digits = CobolUtils.padLeftZeros(amount.abs().movePointRight(2).toBigInteger(), 11);
        String integerDigits = digits.substring(0, 9);
        String decimals = digits.substring(9);

        StringBuilder grouped = new StringBuilder(integerDigits.substring(0, 3) + ","
                + integerDigits.substring(3, 6) + "," + integerDigits.substring(6, 9));
        for (int i = 0; i < grouped.length(); i++) {
            char current = grouped.charAt(i);
            if (current != '0' && current != ',') {
                break;
            }
            grouped.setCharAt(i, ' ');
        }

        char sign = amount.signum() < 0 ? '-' : (signSymbol == '+' ? '+' : ' ');
        return sign + grouped.toString() + "." + decimals;
    }

    private String pad(String line) {
        return CobolUtils.padRight(line, RECORD_LENGTH);
    }
}
