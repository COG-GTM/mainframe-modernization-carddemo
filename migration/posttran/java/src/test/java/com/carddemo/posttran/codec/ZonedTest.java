package com.carddemo.posttran.codec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sign handling is the failure mode that makes a careless port produce plausible, wrong money: the
 * cases below are bytes actually present in the drop's data files and in the legacy program's
 * output, and the negative ones are exactly what GnuCOBOL misreads as positive when the program is
 * compiled without {@code -fsign=EBCDIC}.
 */
class ZonedTest {

    @ParameterizedTest(name = "\"{0}\" decodes to {1}")
    @CsvSource({
            "'0000000000{',      0.00",   // +0
            "'0000060890{',   6089.00",
            "'00000060890',    608.90",   // plain trailing digit: unsigned, positive
            "'0000000030}',     -3.00",
            "'0000012345J',  -1234.51",
            "'0000000000}',      0.00",   // -0 is still zero
            "'0000000009R',     -0.99",
            "'0000000000I',      0.09",
    })
    void decodesOverpunchedAmounts(String field, BigDecimal expected) {
        assertThat(Zoned.decode(field, 2)).isEqualByComparingTo(expected);
    }

    @ParameterizedTest(name = "{0} encodes to \"{1}\"")
    @CsvSource({
            "    0.00, '0000000000{'",
            " 6089.00, '0000060890{'",
            "   -3.00, '0000000030}'",
            "-1234.51, '0000012345J'",
            "   -0.99, '0000000009R'",
    })
    void encodesTheSignIntoTheLastByte(BigDecimal value, String expected) {
        assertThat(Zoned.encode(value, 11, 2)).isEqualTo(expected);
    }

    @Test
    void roundTripsEveryOverpunchDigit() {
        for (int digit = 0; digit <= 9; digit++) {
            BigDecimal positive = new BigDecimal("100.0" + digit);
            BigDecimal negative = positive.negate();
            assertThat(Zoned.decode(Zoned.encode(positive, 11, 2), 2)).isEqualByComparingTo(positive);
            assertThat(Zoned.decode(Zoned.encode(negative, 11, 2), 2)).isEqualByComparingTo(negative);
        }
    }

    @Test
    void decodesTheUnsignedIntegerFieldsTheRecordsAlsoContain() {
        assertThat(Zoned.decode("000000010", 0)).isEqualByComparingTo("10");
        assertThat(Zoned.decode("0", 0)).isEqualByComparingTo("0");
    }

    @Test
    void rejectsFieldsThatAreNotZonedDecimal() {
        assertThatThrownBy(() -> Zoned.decode("00000000*", 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Zoned.decode("", 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
