package com.carddemo.batch.posting;

import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * COBOL program: CBTRN02C — daily transaction posting (JCL POSTTRAN).
 *
 * <p>Rendering side of the fixed-width layouts consumed by {@code com.carddemo.util.CobolUtils}:
 * the DALYREJS file (RECFM=F, LRECL=430 in POSTTRAN.jcl) carries the raw 350-byte DALYTRAN record
 * (copybook CVTRA06Y) followed by the 80-byte validation trailer, so the daily transaction has to
 * be written back out exactly as it was read.
 */
final class CobolRecordFormat {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private CobolRecordFormat() {
    }

    /**
     * Renders a signed zoned-decimal {@code PIC S9(m)V9(scale)} field of {@code digits} total
     * digits, with the sign carried by the overpunch character in the last byte.
     */
    static String zoned(BigDecimal value, int digits, int scale) {
        BigDecimal amount = CobolUtils.nvl(value).setScale(scale, RoundingMode.HALF_UP);
        boolean negative = amount.signum() < 0;
        String unsigned = CobolUtils.padLeftZeros(amount.abs().movePointRight(scale).toBigInteger(), digits);
        int lastDigit = unsigned.charAt(digits - 1) - '0';
        String overpunch = negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH;
        return unsigned.substring(0, digits - 1) + overpunch.charAt(lastDigit);
    }

    /** PIC X(n): left justified, space padded, truncated to length. */
    static String alpha(String value, int length) {
        return CobolUtils.padRight(value, length);
    }

    /** PIC 9(n): right justified, zero padded. */
    static String unsigned(Object value, int length) {
        return CobolUtils.padLeftZeros(value == null ? 0 : value, length);
    }
}
