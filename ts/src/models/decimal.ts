/**
 * Helpers for COBOL fixed-point arithmetic.
 *
 * COBOL computes on scaled integers (PIC S9(9)V99 holds hundredths), so results
 * never accumulate binary floating point drift. These helpers reproduce that by
 * scaling to integers before adding/multiplying and rounding half-up, which is
 * what `ROUNDED` does in the CardDemo programs.
 */

export function roundToScale(value: number, scale = 2): number {
  const factor = 10 ** scale;
  const scaled = value * factor;
  const rounded =
    Math.sign(scaled) * Math.round(Math.abs(scaled) + Number.EPSILON * Math.abs(scaled));
  return rounded / factor;
}

/** Truncates towards zero, matching COBOL COMPUTE without ROUNDED. */
export function truncateToScale(value: number, scale = 2): number {
  const factor = 10 ** scale;
  return Math.trunc(value * factor + Math.sign(value) * 1e-9) / factor;
}

export function addMoney(...values: number[]): number {
  return roundToScale(values.reduce((total, value) => total + Math.round(value * 100), 0) / 100, 2);
}

export function subtractMoney(minuend: number, subtrahend: number): number {
  return addMoney(minuend, -subtrahend);
}

/** Multiplies a monetary amount by a rate and rounds to cents (ROUNDED). */
export function multiplyMoney(amount: number, rate: number): number {
  return roundToScale(amount * rate, 2);
}
