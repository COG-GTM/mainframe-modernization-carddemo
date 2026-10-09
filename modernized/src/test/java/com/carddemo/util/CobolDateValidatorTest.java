package com.carddemo.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CobolDateValidatorTest {

    @Test
    void acceptsValidIsoDate() {
        CobolDateValidator.Result result = CobolDateValidator.validate("2024-02-29", "YYYY-MM-DD");

        assertThat(result.isValid()).isTrue();
        assertThat(result.message()).isEqualTo("Date is valid");
    }

    @Test
    void rejectsImpossibleDate() {
        assertThat(CobolDateValidator.validate("2023-02-29", "YYYY-MM-DD").isValid()).isFalse();
        assertThat(CobolDateValidator.validate("2023-13-01", "YYYY-MM-DD").isValid()).isFalse();
    }

    @Test
    void rejectsUnknownMaskAndEmptyInput() {
        assertThat(CobolDateValidator.validate("2023-01-01", "YY-MM").message()).isEqualTo("Bad Pic String");
        assertThat(CobolDateValidator.validate("", "YYYY-MM-DD").message()).isEqualTo("Insufficient");
    }
}
