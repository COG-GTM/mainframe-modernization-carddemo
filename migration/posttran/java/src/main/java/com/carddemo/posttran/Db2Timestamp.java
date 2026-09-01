package com.carddemo.posttran;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * {@code Z-GET-DB2-FORMAT-TIMESTAMP} — CBTRN02C.cbl:692-705.
 *
 * <p>Assembles {@code EEEE-MM-DD-UU.MM.SS.HH0000} from {@code FUNCTION CURRENT-DATE}: the
 * sub-second part is the two hundredths-of-a-second digits COBOL supplies, padded with a literal
 * {@code '0000'}, not microseconds.
 *
 * <p>The clock is injected so the parity harness can pin it; with the wall clock this field is the
 * one source of non-determinism in the whole step and every posted record differs run to run.
 */
public final class Db2Timestamp {

    private final Clock clock;

    public Db2Timestamp(Clock clock) {
        this.clock = clock;
    }

    public String now() {
        return format(LocalDateTime.now(clock));
    }

    public static String format(LocalDateTime t) {
        int hundredths = t.getNano() / 10_000_000;
        return "%04d-%02d-%02d-%02d.%02d.%02d.%02d0000".formatted(
                t.getYear(), t.getMonthValue(), t.getDayOfMonth(),
                t.getHour(), t.getMinute(), t.getSecond(), hundredths);
    }
}
