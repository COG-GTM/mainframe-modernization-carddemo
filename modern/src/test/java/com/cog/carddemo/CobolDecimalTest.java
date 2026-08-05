package com.cog.carddemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CobolDecimalTest {

    @ParameterizedTest
    @CsvSource({
        "00000001940{, 194.00",
        "000000019400, 194.00",
        "00000020200{, 2020.00",
        "00000000000{, 0.00",
        "00000000000}, 0.00",
        "00000000102J, -10.21",
        "00000010250}, -1025.00",
        "00000000000A, 0.01",
        "999999999999, 9999999999.99"
    })
    void parsesZonedDecimalWithBothSignConventions(String field, String expected) {
        assertEquals(new BigDecimal(expected), CobolDecimal.parseZoned(field, 2));
    }

    @Test
    void keepsTheDeclaredScaleSoThatAmountsCompareExactly() {
        BigDecimal value = CobolDecimal.parseZoned("00000001940{", 2);

        assertEquals(2, value.scale());
        assertEquals(new BigDecimal("194.00"), value);
    }

    @ParameterizedTest
    @CsvSource({
        "194.00, 000000019400",
        "0.00, 000000000000",
        "-1025.00, 00000010250}",
        "-0.01, 00000000000J",
        "9999999999.99, 999999999999"
    })
    void formatsZonedDecimalWithTheAsciiSignConvention(String value, String expected) {
        assertEquals(expected, CobolDecimal.formatZoned(new BigDecimal(value), 12, 2));
    }

    @ParameterizedTest
    @CsvSource({
        "194.00, +0000000194.00",
        "0.00, +0000000000.00",
        "-1025.00, -0000001025.00"
    })
    void rendersDisplayOutputLikeCobol(String value, String expected) {
        assertEquals(expected, CobolDecimal.formatDisplay(new BigDecimal(value), 12, 2));
    }

    @Test
    void roundTripsEveryZonedValueOfTheSampleDataConvention() {
        for (int cents = -5000; cents <= 5000; cents += 7) {
            BigDecimal value = BigDecimal.valueOf(cents, 2);
            String field = CobolDecimal.formatZoned(value, 12, 2);

            assertEquals(12, field.length());
            assertEquals(value, CobolDecimal.parseZoned(field, 2));
        }
    }

    @Test
    void rejectsFieldsThatAreNotValidZonedDecimals() {
        assertThrows(IllegalArgumentException.class, () -> CobolDecimal.parseZoned("00000A01940{", 2));
        assertThrows(IllegalArgumentException.class, () -> CobolDecimal.parseZoned("00000001940?", 2));
        assertThrows(IllegalArgumentException.class, () -> CobolDecimal.parseZoned("", 2));
    }

    @Test
    void rejectsValuesThatDoNotFitTheCobolPicture() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CobolDecimal.formatZoned(new BigDecimal("10000000000.00"), 12, 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> CobolDecimal.formatZoned(new BigDecimal("1.005"), 12, 2));
    }
}
