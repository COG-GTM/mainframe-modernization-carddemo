package com.carddemo.batch.readers;

import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * COBOL programs: CBACT01C, CBACT02C, CBACT03C, CBCUS01C — shared rendering of the
 * {@code DISPLAY} statements of the sequential VSAM reader programs.
 *
 * <p>Reproduces how the mainframe writes a record to SYSOUT: fixed-width, zoned-decimal
 * fields exactly as they are held in the VSAM records (copybooks CVACT01Y, CVACT02Y,
 * CVACT03Y, CVCUS01Y), and the {@code 9910-DISPLAY-IO-STATUS} file-status formatting.
 */
final class CobolDisplay {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private CobolDisplay() {
    }

    /**
     * Renders a {@code PIC S9(integerDigits)V9(scale)} field the way it is stored on the file:
     * zero padded digits with the sign carried as an overpunch in the last byte.
     */
    static String zoned(BigDecimal value, int integerDigits, int scale) {
        BigDecimal scaled = CobolUtils.nvl(value).setScale(scale, RoundingMode.HALF_UP);
        boolean negative = scaled.signum() < 0;
        int totalDigits = integerDigits + scale;
        String digits = CobolUtils.padLeftZeros(
                scaled.abs().movePointRight(scale).toBigInteger().toString(), totalDigits);
        int lastDigit = digits.charAt(totalDigits - 1) - '0';
        char overpunch = negative
                ? NEGATIVE_OVERPUNCH.charAt(lastDigit)
                : POSITIVE_OVERPUNCH.charAt(lastDigit);
        return digits.substring(0, totalDigits - 1) + overpunch;
    }

    /** Renders a {@code PIC X(n)} field: left justified, space padded. */
    static String text(String value, int length) {
        return CobolUtils.padRight(value, length);
    }

    /** Renders a {@code PIC 9(n)} field: right justified, zero padded. */
    static String number(Number value, int length) {
        return CobolUtils.padLeftZeros(value == null ? 0 : value, length);
    }

    /**
     * {@code 9910-DISPLAY-IO-STATUS} / {@code Z-DISPLAY-IO-STATUS}: a non numeric status or a
     * status starting with '9' is shown as the first status byte followed by the binary value
     * of the second byte in three digits; every other status is shown as {@code 00} + status.
     */
    static String ioStatus(String status) {
        String value = CobolUtils.padRight(status, 2);
        char stat1 = value.charAt(0);
        char stat2 = value.charAt(1);
        boolean numeric = Character.isDigit(stat1) && Character.isDigit(stat2);
        if (!numeric || stat1 == '9') {
            return "FILE STATUS IS: NNNN" + stat1 + CobolUtils.padLeftZeros((int) stat2, 3);
        }
        return "FILE STATUS IS: NNNN" + "00" + value;
    }
}
