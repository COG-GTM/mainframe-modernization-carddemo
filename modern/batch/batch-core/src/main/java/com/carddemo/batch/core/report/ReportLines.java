package com.carddemo.batch.core.report;

import com.carddemo.batch.core.FixedWidth;
import java.util.Arrays;

/**
 * Constant lines of the CBTRN03C report ({@code app/cpy/CVTRA07Y.cpy}) and the 133-byte
 * {@code FD-REPTFILE-REC} length. Shorter copybook groups are space-padded to 133 bytes, as a MOVE
 * of a group into {@code FD-REPTFILE-REC PIC X(133)} does.
 */
public final class ReportLines {

    public static final int LENGTH = 133;
    public static final String AMOUNT_PICTURE = "-ZZZ,ZZZ,ZZZ.ZZ";
    public static final String TOTAL_PICTURE = "+ZZZ,ZZZ,ZZZ.ZZ";

    private static final byte[] HEADER_1 = FixedWidth.encode(FixedWidth.fit(
            FixedWidth.fit("Transaction ID", 17)
                    + FixedWidth.fit("Account ID", 12)
                    + FixedWidth.fit("Transaction Type", 19)
                    + FixedWidth.fit("Tran Category", 35)
                    + FixedWidth.fit("Tran Source", 14)
                    + " "
                    + FixedWidth.fit("        Amount", 16),
            LENGTH));
    private static final byte[] HEADER_2 = FixedWidth.encode("-".repeat(LENGTH));
    private static final byte[] BLANK = FixedWidth.encode(" ".repeat(LENGTH));

    private ReportLines() {
    }

    /** {@code TRANSACTION-HEADER-1}: column titles. */
    public static byte[] transactionHeader1() {
        return HEADER_1.clone();
    }

    /** {@code TRANSACTION-HEADER-2 PIC X(133) VALUE ALL '-'}: the separator line. */
    public static byte[] transactionHeader2() {
        return HEADER_2.clone();
    }

    /** {@code WS-BLANK-LINE}: 133 spaces. */
    public static byte[] blankLine() {
        return BLANK.clone();
    }

    static byte[] requireLength(byte[] bytes) {
        if (bytes.length != LENGTH) {
            throw new IllegalArgumentException("Expected a " + LENGTH + "-byte report line but got " + bytes.length);
        }
        return Arrays.copyOf(bytes, LENGTH);
    }
}
