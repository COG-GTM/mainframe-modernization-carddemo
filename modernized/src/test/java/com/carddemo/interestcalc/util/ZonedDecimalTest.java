package com.carddemo.interestcalc.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZonedDecimalTest {

    @ParameterizedTest(name = "\"{0}\" scale {1} -> {2}")
    @CsvSource({
            "'0000000000{', 2, 0.00",        // TRAN-CAT-BAL S9(09)V99, +0
            "'0000005000{', 2, 500.00",
            "'000000500A', 2, 50.01",        // A = +1
            "'000000500I', 2, 50.09",        // I = +9
            "'0000005000}', 2, -500.00",     // } = -0
            "'000000500J', 2, -50.01",       // J = -1
            "'000000500R', 2, -50.09",       // R = -9
            "'00150{', 2, 15.00",            // DIS-INT-RATE S9(04)V99 from discgrp.txt
            "'00000001940{', 2, 194.00",    // ACCT-CURR-BAL S9(10)V99 from acctdata.txt
            "'123456', 0, 123456",           // plain trailing digit (unsigned)
    })
    void parsesZonedDecimals(String text, int scale, BigDecimal expected) {
        assertThat(ZonedDecimal.parse(text, scale)).isEqualByComparingTo(expected);
    }

    @Test
    void rejectsInvalidSignCharacter() {
        assertThatThrownBy(() -> ZonedDecimal.parse("00001*", 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0} -> \"{3}\"")
    @CsvSource({
            "12.50,   11, 2, '0000000125{'",
            "-12.50,  11, 2, '0000000125}'",
            "0.99,    11, 2, '0000000009I'",
            "-0.99,   11, 2, '0000000009R'",
            "0.00,    11, 2, '0000000000{'",
    })
    void formatsZonedDecimals(BigDecimal value, int totalDigits, int scale, String expected) {
        assertThat(ZonedDecimal.format(value, totalDigits, scale)).isEqualTo(expected);
    }

    @Test
    void formatThenParseRoundTrips() {
        BigDecimal value = new BigDecimal("-1234567.89");
        String formatted = ZonedDecimal.format(value, 11, 2);
        assertThat(ZonedDecimal.parse(formatted, 2)).isEqualByComparingTo(value);
    }
}
