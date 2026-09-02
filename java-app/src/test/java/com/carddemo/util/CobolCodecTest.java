package com.carddemo.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CobolCodecTest {

    @Test
    void decodesPositiveOverpunchedSign() {
        assertThat(CobolCodec.decodeSigned("00000001940{", 2)).isEqualByComparingTo("194.00");
        assertThat(CobolCodec.decodeSigned("0000005047G", 2)).isEqualByComparingTo("504.77");
    }

    @Test
    void decodesNegativeOverpunchedSign() {
        assertThat(CobolCodec.decodeSigned("00000001940}", 2)).isEqualByComparingTo("-194.00");
        assertThat(CobolCodec.decodeSigned("0000000194J", 2)).isEqualByComparingTo("-19.41");
    }

    @Test
    void preservesScaleOfDecodedValues() {
        assertThat(CobolCodec.decodeSigned("00000000000{", 2).scale()).isEqualTo(2);
        assertThat(CobolCodec.decodeSigned("            ", 2)).isEqualByComparingTo("0.00");
    }

    @Test
    void roundTripsSignedValues() {
        for (String field : new String[] {"00000001940{", "00000001940}", "00000000000{", "0000000000A"}) {
            int scale = 2;
            BigDecimal value = CobolCodec.decodeSigned(field, scale);
            assertThat(CobolCodec.encodeSigned(value, field.length(), scale)).isEqualTo(field);
        }
    }

    @Test
    void encodesNegativeZeroAsPositiveZero() {
        assertThat(CobolCodec.encodeSigned(new BigDecimal("0.00"), 12, 2)).isEqualTo("00000000000{");
    }

    @Test
    void rejectsValuesWiderThanThePictureClause() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> CobolCodec.encodeSigned(new BigDecimal("12345678901.23"), 12, 2)))
                .hasMessageContaining("does not fit");
    }
}
