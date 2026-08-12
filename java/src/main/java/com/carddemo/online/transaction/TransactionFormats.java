package com.carddemo.online.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * COBOL programs: COTRN00C / COTRN01C / COTRN02C — edited picture clauses used by the CT00/CT01/CT02
 * screens (BMS maps COTRN00 / COTRN01 / COTRN02).
 */
public final class TransactionFormats {

    /** Length of TRAN-ID (CVTRA05Y, PIC X(16) holding digits). */
    public static final int TRAN_ID_LENGTH = 16;

    /** Browse key equivalent of MOVE LOW-VALUES TO TRAN-ID. */
    public static final String LOW_VALUES_KEY = " ".repeat(TRAN_ID_LENGTH);

    /**
     * Browse key equivalent of MOVE HIGH-VALUES TO TRAN-ID. TRAN-ID always holds 16 digits, so the
     * all-nines key is the highest key the file can contain.
     */
    public static final String HIGH_VALUES_KEY = "9".repeat(TRAN_ID_LENGTH);

    private TransactionFormats() {}

    /** Renders WS-TRAN-AMT PIC +99999999.99. */
    public static String amount(BigDecimal value) {
        BigDecimal scaled = (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
        BigDecimal absolute = scaled.abs();
        String digits = absolute.unscaledValue().toString();
        if (digits.length() < 3) {
            digits = "0".repeat(3 - digits.length()) + digits;
        }
        String integerPart = digits.substring(0, digits.length() - 2);
        String decimalPart = digits.substring(digits.length() - 2);
        if (integerPart.length() < 8) {
            integerPart = "0".repeat(8 - integerPart.length()) + integerPart;
        } else if (integerPart.length() > 8) {
            integerPart = integerPart.substring(integerPart.length() - 8);
        }
        return (scaled.signum() < 0 ? "-" : "+") + integerPart + "." + decimalPart;
    }

    /**
     * Renders WS-TRAN-DATE PIC X(08) as MM/DD/YY, taken from the first ten characters of a
     * TRAN-ORIG-TS (PIC X(26), {@code yyyy-MM-dd-HH.mm.ss.nnnnnn}) value.
     */
    public static String listDate(String originTimestamp) {
        if (originTimestamp == null || originTimestamp.length() < 10) {
            return "00/00/00";
        }
        String yy = originTimestamp.substring(2, 4);
        String mm = originTimestamp.substring(5, 7);
        String dd = originTimestamp.substring(8, 10);
        return mm + "/" + dd + "/" + yy;
    }

    /** Right-justified zero padded 16 digit TRAN-ID. */
    public static String transactionId(long value) {
        return String.format("%016d", value);
    }
}
