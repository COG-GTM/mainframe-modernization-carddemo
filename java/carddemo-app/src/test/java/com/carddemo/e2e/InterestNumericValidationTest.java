package com.carddemo.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.batch.account.InterestCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * CS-15 numeric validation of the monthly-interest arithmetic against the legacy
 * {@code CBACT04C 1300-COMPUTE-INTEREST}:
 *
 * <pre>
 *   COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 * </pre>
 *
 * with {@code WS-MONTHLY-INT PIC S9(09)V99} (CBACT04C line 168). The receiving field has two
 * decimals and the {@code COMPUTE} has <em>no {@code ROUNDED} phrase</em>, so COBOL
 * <em>truncates</em> toward zero to scale 2 — reproduced by
 * {@link InterestCalculator#monthlyInterest} with {@link java.math.RoundingMode#DOWN}. Each
 * expected constant below is computed by hand from the formula and cites why truncation (not
 * rounding) is the correct behaviour.
 *
 * <p>This is a pure unit test (no Spring context) so the exact BigDecimal contract is pinned
 * fast and independently of the batch wiring exercised in {@link BatchGoldenPathE2ETest}.</p>
 */
class InterestNumericValidationTest {

    /** 1234.56 * 18.00 / 1200 = 18.51840; truncated to scale 2 → 18.51 (HALF_UP would also give 18.51). */
    @Test
    void truncatesFractionBelowHalf() {
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("1234.56"), new BigDecimal("18.00")))
                .isEqualByComparingTo("18.51");
    }

    /**
     * 100.00 * 23.00 / 1200 = 1.916666…; COBOL truncates → 1.91. This is the discriminating case:
     * HALF_UP rounding would yield 1.92, so a passing assertion proves truncation semantics.
     */
    @Test
    void truncatesFractionAboveHalfWithoutRounding() {
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("100.00"), new BigDecimal("23.00")))
                .isEqualByComparingTo("1.91");
    }

    /** 500.00 * 24.00 / 1200 = 10.0000 → 10.00 exactly. */
    @Test
    void exactResultKeepsScaleTwo() {
        BigDecimal result = InterestCalculator.monthlyInterest(
                new BigDecimal("500.00"), new BigDecimal("24.00"));
        assertThat(result).isEqualByComparingTo("10.00");
        assertThat(result.scale()).isEqualTo(2);
    }

    /** 170.00 * 12.00 / 1200 = 1.70 — the exact value chained through {@link BatchGoldenPathE2ETest}. */
    @Test
    void matchesBatchGoldenPathInterest() {
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("170.00"), new BigDecimal("12.00")))
                .isEqualByComparingTo("1.70");
    }

    /** 250.00 * 12.00 / 1200 = 2.50 (the DEFAULT-group fallback rate case). */
    @Test
    void defaultGroupRateCase() {
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("250.00"), new BigDecimal("12.00")))
                .isEqualByComparingTo("2.50");
    }

    /**
     * A tiny balance whose interest is below one cent truncates to 0.00: 0.50 * 12.00 / 1200 =
     * 0.005 → 0.00. In {@code CBACT04C} the {@code IF DIS-INT-RATE NOT = 0} guard still fires
     * (the rate is non-zero) but the computed interest is zero.
     */
    @Test
    void subCentInterestTruncatesToZero() {
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("0.50"), new BigDecimal("12.00")))
                .isEqualByComparingTo("0.00");
    }
}
