package com.carddemo.etl.codec;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignOverpunchDecoderTest {

    @Test
    void decodesPositiveZeroOverpunch() {
        assertThat(SignOverpunchDecoder.decode("00000001940{", 2)).isEqualByComparingTo("194.00");
    }

    @Test
    void decodesNegativeZeroOverpunch() {
        assertThat(SignOverpunchDecoder.decode("00000001940}", 2)).isEqualByComparingTo("-194.00");
    }

    @Test
    void decodesPositiveDigitOverpunch() {
        // 'A'..'I' map to digits 1..9 with a positive sign.
        assertThat(SignOverpunchDecoder.decode("12345A", 0)).isEqualByComparingTo("123451");
        assertThat(SignOverpunchDecoder.decode("12345I", 0)).isEqualByComparingTo("123459");
    }

    @Test
    void decodesNegativeDigitOverpunch() {
        // 'J'..'R' map to digits 1..9 with a negative sign.
        assertThat(SignOverpunchDecoder.decode("12345J", 0)).isEqualByComparingTo("-123451");
        assertThat(SignOverpunchDecoder.decode("12345R", 0)).isEqualByComparingTo("-123459");
    }

    @Test
    void appliesImpliedDecimalScale() {
        assertThat(SignOverpunchDecoder.decode("0000000000{", 2)).isEqualByComparingTo("0.00");
        assertThat(SignOverpunchDecoder.decode("00000012345E", 2))
                .isEqualByComparingTo("1234.55"); // E = +5
    }

    @Test
    void negativeZeroIsZero() {
        BigDecimal value = SignOverpunchDecoder.decode("0000000000}", 2);
        assertThat(value).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsNonDigitInLeadingPosition() {
        assertThatThrownBy(() -> SignOverpunchDecoder.decode("12X45A", 0))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void rejectsUnknownOverpunchCharacter() {
        assertThatThrownBy(() -> SignOverpunchDecoder.decode("12345*", 0))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void rejectsEmptyField() {
        assertThatThrownBy(() -> SignOverpunchDecoder.decode("", 2))
                .isInstanceOf(NumberFormatException.class);
    }
}
