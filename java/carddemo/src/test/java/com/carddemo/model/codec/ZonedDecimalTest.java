package com.carddemo.model.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZonedDecimalTest {

    @ParameterizedTest
    @CsvSource({
            "00000001940{, 194.00",
            "00000001940}, -194.00",
            "00000000000{, 0.00",
            "00000000000A, 0.01",
            "00000000000J, -0.01",
            "00000000123D, 12.34"
    })
    void decodesOverpunchedSign(String raw, BigDecimal expected) {
        assertThat(ZonedDecimal.decodeSigned(raw, 2)).isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "194.00, 00000001940{",
            "-194.00, 00000001940}",
            "0.00, 00000000000{",
            "-0.01, 00000000000J"
    })
    void encodesOverpunchedSign(BigDecimal value, String expected) {
        assertThat(ZonedDecimal.encodeSigned(value, 12, 2)).isEqualTo(expected);
    }

    @Test
    void treatsBlankSignedFieldAsAbsent() {
        assertThat(ZonedDecimal.decodeSigned("            ", 2)).isNull();
    }

    @Test
    void rejectsValuesWiderThanThePicClause() {
        assertThatThrownBy(() -> ZonedDecimal.encodeSigned(new BigDecimal("1234.56"), 4, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zeroFillsUnsignedFields() {
        assertThat(ZonedDecimal.encodeUnsigned(42L, 11)).isEqualTo("00000000042");
        assertThat(ZonedDecimal.decodeUnsigned("00000000042")).isEqualTo(42L);
        assertThat(ZonedDecimal.decodeUnsigned("           ")).isNull();
    }
}
