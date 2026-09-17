/**
 * Fixed-point decimal arithmetic matching COBOL COMPUTE/ADD semantics.
 *
 * COBOL numeric fields are fixed-point (PIC S9(n)V99), not binary floating point.
 * All arithmetic here is performed on scaled integers (bigint) so that results are
 * exact, and rounding follows COBOL `ROUNDED` (half-up, away from zero).
 */

const POW10: bigint[] = Array.from({ length: 32 }, (_, i) => 10n ** BigInt(i));

function pow10(scale: number): bigint {
  const cached = POW10[scale];
  if (cached !== undefined) return cached;
  return 10n ** BigInt(scale);
}

/** Converts a JS number holding a fixed-point value into scaled integer units. */
export function toUnits(value: number, scale: number): bigint {
  if (!Number.isFinite(value)) throw new RangeError(`non-finite value: ${value}`);
  const factor = Number(pow10(scale));
  const scaled = Math.round(value * factor);
  if (!Number.isSafeInteger(scaled)) {
    throw new RangeError(`value ${value} with scale ${scale} exceeds safe integer range`);
  }
  return BigInt(scaled);
}

/** Converts scaled integer units back into a JS number. */
export function fromUnits(units: bigint, scale: number): number {
  const factor = pow10(scale);
  const whole = units / factor;
  const rest = units % factor;
  if (!Number.isSafeInteger(Number(whole))) {
    throw new RangeError(`units ${units} exceed safe integer range`);
  }
  return Number(whole) + Number(rest) / Number(factor);
}

/** Divides scaled units, rounding half-up away from zero (COBOL ROUNDED). */
function divRoundHalfUp(numerator: bigint, denominator: bigint): bigint {
  if (denominator === 0n) throw new RangeError('division by zero');
  const negative = numerator < 0n !== denominator < 0n;
  const absNum = numerator < 0n ? -numerator : numerator;
  const absDen = denominator < 0n ? -denominator : denominator;
  const quotient = (2n * absNum + absDen) / (2n * absDen);
  return negative ? -quotient : quotient;
}

/** Rounds a value to `scale` decimal places using COBOL ROUNDED (half-up, away from zero). */
export function roundHalfUp(value: number, scale: number): number {
  const factor = Number(pow10(scale));
  const scaled = value * factor;
  const rounded = scaled < 0 ? -Math.round(-scaled) : Math.round(scaled);
  return rounded / factor;
}

/** Truncates a value to `scale` decimal places (COBOL default, no ROUNDED phrase). */
export function truncate(value: number, scale: number): number {
  const factor = Number(pow10(scale));
  const scaled = value * factor;
  const truncated = scaled < 0 ? Math.ceil(scaled) : Math.floor(scaled);
  return truncated / factor;
}

/** Exact addition of two fixed-point values at `scale` decimal places. */
export function addDecimal(a: number, b: number, scale = 2): number {
  return fromUnits(toUnits(a, scale) + toUnits(b, scale), scale);
}

/** Exact subtraction of two fixed-point values at `scale` decimal places. */
export function subtractDecimal(a: number, b: number, scale = 2): number {
  return fromUnits(toUnits(a, scale) - toUnits(b, scale), scale);
}

/** Exact sum of fixed-point values at `scale` decimal places. */
export function sumDecimal(values: readonly number[], scale = 2): number {
  let units = 0n;
  for (const value of values) units += toUnits(value, scale);
  return fromUnits(units, scale);
}

/**
 * Multiplies two fixed-point values and rounds the product to `resultScale`
 * decimal places (half-up), as COBOL `COMPUTE ... ROUNDED` does.
 */
export function multiplyDecimal(a: number, b: number, scale = 2, resultScale = scale): number {
  const product = toUnits(a, scale) * toUnits(b, scale);
  const shift = pow10(2 * scale - resultScale);
  return fromUnits(divRoundHalfUp(product, shift), resultScale);
}

/**
 * Divides two fixed-point values and rounds the quotient to `resultScale`
 * decimal places (half-up), as COBOL `COMPUTE ... ROUNDED` does.
 */
export function divideDecimal(a: number, b: number, scale = 2, resultScale = scale): number {
  const numerator = toUnits(a, scale) * pow10(resultScale);
  const denominator = toUnits(b, scale);
  return fromUnits(divRoundHalfUp(numerator, denominator), resultScale);
}

/** Compares two fixed-point values exactly at `scale` decimal places. */
export function compareDecimal(a: number, b: number, scale = 2): -1 | 0 | 1 {
  const left = toUnits(a, scale);
  const right = toUnits(b, scale);
  if (left < right) return -1;
  if (left > right) return 1;
  return 0;
}
