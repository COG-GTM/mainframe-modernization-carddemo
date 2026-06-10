package com.carddemo.interestcalc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure interest computation extracted from CBACT04C paragraph {@code 1300-COMPUTE-INTEREST}.
 *
 * <p>COBOL source:
 * <pre>
 *   COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 * </pre>
 *
 * <p>Fixed-point semantics preserved:
 * <ul>
 *   <li>{@code TRAN-CAT-BAL} is {@code PIC S9(09)V99} (scale 2) and {@code DIS-INT-RATE} is
 *       {@code PIC S9(04)V99} (scale 2); their product is an exact scale-4 intermediate.</li>
 *   <li>The COMPUTE has <b>no ROUNDED phrase</b>, so storing into
 *       {@code WS-MONTHLY-INT PIC S9(09)V99} truncates excess decimal digits toward zero.
 *       This maps to {@link RoundingMode#DOWN} at scale 2.</li>
 * </ul>
 */
public final class InterestCalculator {

    /** Divisor converting an annual percentage rate to a monthly fraction (12 months x 100 percent). */
    public static final BigDecimal ANNUAL_PERCENT_TO_MONTHLY = new BigDecimal("1200");

    public static final int MONEY_SCALE = 2;

    private InterestCalculator() {
    }

    /**
     * Computes the monthly interest for one transaction category balance.
     *
     * @param categoryBalance {@code TRAN-CAT-BAL  PIC S9(09)V99}
     * @param annualRate      {@code DIS-INT-RATE  PIC S9(04)V99} (annual percentage rate)
     * @return {@code WS-MONTHLY-INT PIC S9(09)V99} — truncated (not rounded) to 2 decimals,
     *         exactly as the un-ROUNDED COBOL COMPUTE behaves
     */
    public static BigDecimal monthlyInterest(BigDecimal categoryBalance, BigDecimal annualRate) {
        return categoryBalance.multiply(annualRate)
                .divide(ANNUAL_PERCENT_TO_MONTHLY, MONEY_SCALE, RoundingMode.DOWN);
    }
}
