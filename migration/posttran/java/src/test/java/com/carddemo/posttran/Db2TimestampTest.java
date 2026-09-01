package com.carddemo.posttran;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code Z-GET-DB2-FORMAT-TIMESTAMP} builds its sub-second part from the two hundredths-of-a-second
 * digits of {@code FUNCTION CURRENT-DATE} followed by a literal {@code '0000'} (CBTRN02C.cbl:704) —
 * the last four digits are always zeros, not microseconds.
 */
class Db2TimestampTest {

    @Test
    void formatsHundredthsAndPadsTheRemainingFourDigits() {
        assertThat(Db2Timestamp.format(LocalDateTime.of(2024, 3, 1, 12, 34, 56, 789_000_000)))
                .isEqualTo("2024-03-01-12.34.56.780000");
        assertThat(Db2Timestamp.format(LocalDateTime.of(2024, 12, 31, 0, 0, 0, 0)))
                .isEqualTo("2024-12-31-00.00.00.000000");
        assertThat(Db2Timestamp.format(LocalDateTime.of(2024, 1, 9, 9, 8, 7, 990_000_000)))
                .isEqualTo("2024-01-09-09.08.07.990000");
    }

    @Test
    void isDrivenByTheInjectedClockSoRunsAreReproducible() {
        Clock fixed = Clock.fixed(Instant.parse("2024-03-01T12:34:56.78Z"), ZoneOffset.UTC);
        Db2Timestamp timestamp = new Db2Timestamp(fixed);
        assertThat(timestamp.now()).isEqualTo("2024-03-01-12.34.56.780000").isEqualTo(timestamp.now());
    }
}
