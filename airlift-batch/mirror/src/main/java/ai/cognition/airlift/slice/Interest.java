package ai.cognition.airlift.slice;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** The interest arithmetic of CBACT04C:464-465, kept apart so it can be tested directly. */
public final class Interest {

  private static final BigDecimal MONTHS_TIMES_PERCENT = new BigDecimal("1200");

  /** WS-MONTHLY-INT PIC S9(09)V99 (CBACT04C:168). */
  private static final int SCALE = 2;

  private Interest() {}

  /**
   * {@code COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200}.
   *
   * <p>TRAP T1: this rounds the monthly interest to the nearest cent, which
   * contradicts CBACT04C:464-465 - that COMPUTE carries no ROUNDED phrase, so the
   * third decimal of the quotient is dropped rather than rounded. The transcript in
   * airlift-batch/docs/ARITHMETIC.md is GnuCOBOL's own output for both variants and
   * shows the two disagreeing on three of six cases.
   */
  public static BigDecimal monthly(BigDecimal categoryBalance, BigDecimal rate) {
    return categoryBalance.multiply(rate).divide(MONTHS_TIMES_PERCENT, SCALE, RoundingMode.HALF_UP);
  }
}
