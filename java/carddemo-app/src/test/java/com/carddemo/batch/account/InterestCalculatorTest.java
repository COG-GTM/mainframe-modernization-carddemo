package com.carddemo.batch.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Pure-arithmetic tests for {@link InterestCalculator}, pinning the exact COBOL semantics of
 * {@code CBACT04C} paragraph {@code 1300-COMPUTE-INTEREST}
 * ({@code COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200}, no {@code ROUNDED} →
 * truncation into the {@code S9(09)V99} field).
 */
class InterestCalculatorTest {

    @Test
    void truncatesTowardZeroLikeUnroundedCompute() {
        // 1234.56 * 18.00 = 22222.0800 ; / 1200 = 18.5184 -> truncated to scale 2 -> 18.51
        BigDecimal result = InterestCalculator.monthlyInterest(
                new BigDecimal("1234.56"), new BigDecimal("18.00"));
        assertThat(result).isEqualByComparingTo("18.51");
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    void exactWhenNoRemainder() {
        // 500.00 * 24.00 = 12000.0000 ; / 1200 = 10.00 exactly
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("500.00"), new BigDecimal("24.00")))
                .isEqualByComparingTo("10.00");
    }

    @Test
    void zeroBalanceYieldsZero() {
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("0.00"), new BigDecimal("15.00")))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void doesNotRoundHalfUp() {
        // 100.00 * 15.99 = 1599.0000 ; / 1200 = 1.3325 -> truncates to 1.33 (not 1.33 rounded, same)
        // 100.00 * 23.00 = 2300.0000 ; / 1200 = 1.91666... -> truncates to 1.91 (half-up would give 1.92)
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("100.00"), new BigDecimal("23.00")))
                .isEqualByComparingTo("1.91");
    }
}
