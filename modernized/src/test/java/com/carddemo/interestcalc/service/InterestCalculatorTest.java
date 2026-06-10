package com.carddemo.interestcalc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Equivalence tests for the CBACT04C 1300-COMPUTE-INTEREST formula:
 * {@code WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200} with no ROUNDED phrase,
 * i.e. truncation toward zero at 2 decimal places into PIC S9(09)V99.
 */
class InterestCalculatorTest {

    @ParameterizedTest(name = "balance={0} rate={1} -> {2}")
    @CsvSource({
            // simple cases: bal * rate / 1200
            "1200.00, 12.00,  12.00",     // 14400 / 1200 = 12 exactly
            "1000.00, 15.00,  12.50",     // 15000 / 1200 = 12.5 exactly
            "100.00,  12.00,  1.00",
            "0.00,    15.00,  0.00",
            // truncation (NOT half-up rounding) — COBOL COMPUTE without ROUNDED
            "100.00,  11.99,  0.99",      // 1199/1200 = 0.999166... -> 0.99 (round-half-up would give 1.00)
            "999.99,  19.99,  16.65",     // 19989.8001/1200 = 16.6581... -> 16.65 (not 16.66)
            "0.01,    15.00,  0.00",      // 0.15/1200 = 0.000125 -> 0.00
            "1.00,    1.00,   0.00",      // 1/1200 = 0.000833... -> 0.00
            "133.33,  9.00,   0.99",      // 1199.97/1200 = 0.999975 -> 0.99 despite being nearly 1.00
            // negative balances truncate toward zero (COBOL truncates magnitude)
            "-100.00, 11.99,  -0.99",     // -0.999166... -> -0.99 (not -1.00)
            "-0.01,   15.00,  0.00",      // -0.000125 -> 0.00 (negative zero normalizes)
            "-1000.00, 15.00, -12.50",
            // negative rates
            "100.00,  -11.99, -0.99",
            // large balance at PIC S9(09)V99 limits
            "999999999.99, 99.99, 83324999.99", // 99989999999.0001/1200 = 83324999.99916... -> truncated
            "123456789.12, 18.25, 1877572.00",  // 2253086401.44/1200 = 1877572.0012 -> 1877572.00
    })
    void monthlyInterestMatchesCobolTruncation(BigDecimal balance, BigDecimal rate, BigDecimal expected) {
        assertThat(InterestCalculator.monthlyInterest(balance, rate))
                .isEqualByComparingTo(expected);
    }

    @Test
    void resultAlwaysHasScaleTwo() {
        BigDecimal result = InterestCalculator.monthlyInterest(new BigDecimal("1200.00"), new BigDecimal("12.00"));
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    void exactQuotientIsNotAffectedByTruncation() {
        // 600.00 * 2.00 = 1200.00 -> exactly 1.00
        assertThat(InterestCalculator.monthlyInterest(new BigDecimal("600.00"), new BigDecimal("2.00")))
                .isEqualByComparingTo("1.00");
    }

    @Test
    void truncationDiffersFromHalfUpRounding() {
        // 0.999166... would round to 1.00 under HALF_UP; COBOL stores 0.99
        BigDecimal truncated = InterestCalculator.monthlyInterest(new BigDecimal("100.00"), new BigDecimal("11.99"));
        assertThat(truncated).isEqualByComparingTo("0.99");
    }
}
