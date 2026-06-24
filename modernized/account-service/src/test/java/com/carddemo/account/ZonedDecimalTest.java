package com.carddemo.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.account.parser.ZonedDecimal;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ZonedDecimalTest {

    @Test
    void decodesPositiveOverpunchZero() {
        // '{' is the overpunch for "0, positive"
        assertThat(ZonedDecimal.decode("00000001940{", 2)).isEqualByComparingTo("194.00");
    }

    @Test
    void decodesPositiveOverpunchDigits() {
        // 'D' -> 4 positive: 000000000D = 0000000004 -> 0.04 with 2 decimals
        assertThat(ZonedDecimal.decode("000000000D", 2)).isEqualByComparingTo("0.04");
    }

    @Test
    void decodesNegativeOverpunch() {
        // '}' -> 0 negative; 'M' -> 4 negative
        assertThat(ZonedDecimal.decode("00000001234}", 2)).isEqualByComparingTo("-123.40");
        assertThat(ZonedDecimal.decode("0000000123M", 2)).isEqualByComparingTo(new BigDecimal("-12.34"));
    }

    @Test
    void decodesPlainTrailingDigitAsPositive() {
        assertThat(ZonedDecimal.decode("000000012345", 2)).isEqualByComparingTo("123.45");
    }

    @Test
    void rejectsInvalidSign() {
        assertThatThrownBy(() -> ZonedDecimal.decode("0000000000*", 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
