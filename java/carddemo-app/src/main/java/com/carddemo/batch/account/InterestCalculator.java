package com.carddemo.batch.account;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Faithful port of the interest arithmetic of {@code CBACT04C} (INTCALC).
 *
 * <p>The single COBOL statement being reproduced is paragraph {@code 1300-COMPUTE-INTEREST}:</p>
 * <pre>
 *   COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 * </pre>
 *
 * <p>Arithmetic contract (documented so the mapping is auditable):</p>
 * <ul>
 *   <li>{@code TRAN-CAT-BAL} is {@code PIC S9(09)V99} → {@link BigDecimal} scale 2.</li>
 *   <li>{@code DIS-INT-RATE} is {@code PIC S9(04)V99} → {@link BigDecimal} scale 2.</li>
 *   <li>The intermediate product {@code balance * rate} is computed exactly (scale 4).</li>
 *   <li>The division by the literal {@code 1200} stores its result into {@code WS-MONTHLY-INT},
 *       which is {@code PIC S9(09)V99} (scale 2). The {@code COMPUTE} has <b>no {@code ROUNDED}
 *       phrase</b>, so COBOL <b>truncates</b> the fractional part beyond the receiving field's
 *       scale. This is reproduced with {@link RoundingMode#DOWN} at {@link #AMOUNT_SCALE}.</li>
 * </ul>
 *
 * <p>Truncating the exact quotient to 2 decimals is equivalent to computing the quotient to any
 * higher precision and then storing it into the scale-2 field, so a single
 * {@code divide(1200, 2, DOWN)} exactly matches the mainframe result.</p>
 */
public final class InterestCalculator {

    /** Implied decimal scale of the monetary fields ({@code V99}). */
    public static final int AMOUNT_SCALE = 2;

    /** The COBOL literal divisor {@code 1200} (12 months × 100 for the percentage rate). */
    private static final BigDecimal MONTHS_TIMES_HUNDRED = new BigDecimal("1200");

    private InterestCalculator() {
    }

    /**
     * {@code WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200}, truncated to scale 2.
     *
     * @param categoryBalance {@code TRAN-CAT-BAL} for the (account, type, category)
     * @param annualRate      {@code DIS-INT-RATE} (annual percentage, e.g. {@code 18.00})
     * @return the monthly interest amount, scale {@value #AMOUNT_SCALE}, truncated toward zero
     */
    public static BigDecimal monthlyInterest(BigDecimal categoryBalance, BigDecimal annualRate) {
        return categoryBalance.multiply(annualRate)
                .divide(MONTHS_TIMES_HUNDRED, AMOUNT_SCALE, RoundingMode.DOWN);
    }

    /** A zero amount at the monetary scale ({@code 0.00}). */
    public static BigDecimal zeroAmount() {
        return BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }
}
