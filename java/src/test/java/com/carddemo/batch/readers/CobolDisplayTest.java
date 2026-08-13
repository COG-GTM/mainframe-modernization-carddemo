package com.carddemo.batch.readers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Zoned decimal rendering and 9910-DISPLAY-IO-STATUS formatting. */
class CobolDisplayTest {

    @Test
    void rendersSignedZonedDecimalsWithAnOverpunch() {
        assertThat(CobolDisplay.zoned(new BigDecimal("194.00"), 10, 2)).isEqualTo("00000001940{");
        assertThat(CobolDisplay.zoned(new BigDecimal("19.41"), 10, 2)).isEqualTo("00000000194A");
        assertThat(CobolDisplay.zoned(new BigDecimal("-19.49"), 10, 2)).isEqualTo("00000000194R");
        assertThat(CobolDisplay.zoned(new BigDecimal("-0.10"), 10, 2)).isEqualTo("00000000001}");
        assertThat(CobolDisplay.zoned(null, 10, 2)).isEqualTo("00000000000{");
    }

    @Test
    void roundsHalfUpLikeCobolRounded() {
        assertThat(CobolDisplay.zoned(new BigDecimal("1.005"), 10, 2)).isEqualTo("00000000010A");
    }

    @Test
    void formatsFileStatusTheWayThe9910ParagraphDoes() {
        assertThat(CobolDisplay.ioStatus("00")).isEqualTo("FILE STATUS IS: NNNN0000");
        assertThat(CobolDisplay.ioStatus("35")).isEqualTo("FILE STATUS IS: NNNN0035");
        // A '9' status shows the binary value of the second byte in three digits.
        assertThat(CobolDisplay.ioStatus("90")).isEqualTo("FILE STATUS IS: NNNN9048");
        assertThat(CobolDisplay.ioStatus("A1")).isEqualTo("FILE STATUS IS: NNNNA049");
    }
}
