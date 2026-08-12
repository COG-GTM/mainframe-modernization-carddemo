package com.carddemo.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CobolUtilsTest {

    @Test
    void parsesPositiveOverpunchedZonedDecimal() {
        assertThat(CobolUtils.decimal("0000005047G", 2)).isEqualByComparingTo("504.77");
        assertThat(CobolUtils.decimal("00000001940{", 2)).isEqualByComparingTo("194.00");
    }

    @Test
    void parsesNegativeOverpunchedZonedDecimal() {
        assertThat(CobolUtils.decimal("0000009190}", 2)).isEqualByComparingTo("-919.00");
        assertThat(CobolUtils.decimal("0000000012J", 2)).isEqualByComparingTo("-1.21");
    }

    @Test
    void treatsBlankNumericFieldsAsZero() {
        assertThat(CobolUtils.decimal("            ", 2)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(CobolUtils.num("           ", 0, 11)).isZero();
    }

    @Test
    void padsFixedWidthFields() {
        assertThat(CobolUtils.padRight("AB", 5)).isEqualTo("AB   ");
        assertThat(CobolUtils.padLeftZeros(42, 5)).isEqualTo("00042");
    }

    @Test
    void roundsHalfUpLikeCobolRounded() {
        assertThat(CobolUtils.rounded(new BigDecimal("1.005"), 2)).isEqualByComparingTo("1.01");
    }
}
