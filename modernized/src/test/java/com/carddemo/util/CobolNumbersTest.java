package com.carddemo.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CobolNumbersTest {

    @Test
    void parsesPositiveOverpunch() {
        assertThat(CobolNumbers.parseSigned("0000005047G", 2)).isEqualByComparingTo(new BigDecimal("504.77"));
        assertThat(CobolNumbers.parseSigned("00000001940{", 2)).isEqualByComparingTo(new BigDecimal("194.00"));
    }

    @Test
    void parsesNegativeOverpunch() {
        assertThat(CobolNumbers.parseSigned("0000005047P", 2)).isEqualByComparingTo(new BigDecimal("-504.77"));
        assertThat(CobolNumbers.parseSigned("00000001940}", 2)).isEqualByComparingTo(new BigDecimal("-194.00"));
    }

    @Test
    void parsesPlainDigits() {
        assertThat(CobolNumbers.parseSigned("000000504777", 2)).isEqualByComparingTo(new BigDecimal("5047.77"));
        assertThat(CobolNumbers.parseLong("00000000011")).isEqualTo(11L);
        assertThat(CobolNumbers.parseInteger("0001")).isEqualTo(1);
    }
}
