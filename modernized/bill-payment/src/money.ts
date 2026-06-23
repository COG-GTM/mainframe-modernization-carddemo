/**
 * Fixed-point monetary helper.
 *
 * COBOL stores currency as packed/zoned decimals such as `PIC S9(10)V99`
 * (10 integer digits + 2 implied decimal places). Those are exact decimals,
 * never binary floating point. To preserve that behaviour we represent every
 * amount internally as an integer number of cents and only convert to/from a
 * decimal `number` at the edges.
 */
export class Money {
  private constructor(public readonly cents: number) {
    if (!Number.isInteger(cents)) {
      throw new Error(`Money cents must be an integer, got ${cents}`);
    }
  }

  static fromCents(cents: number): Money {
    return new Money(Math.trunc(cents));
  }

  /** Build from a decimal value such as 1234.56 (rounded to the nearest cent). */
  static fromDecimal(value: number): Money {
    return new Money(Math.round(value * 100));
  }

  static zero(): Money {
    return new Money(0);
  }

  add(other: Money): Money {
    return new Money(this.cents + other.cents);
  }

  subtract(other: Money): Money {
    return new Money(this.cents - other.cents);
  }

  isPositive(): boolean {
    return this.cents > 0;
  }

  isZeroOrLess(): boolean {
    return this.cents <= 0;
  }

  /** Decimal value, e.g. 1234.56. */
  toDecimal(): number {
    return this.cents / 100;
  }

  /** Fixed two-decimal string for display / JSON, e.g. "1234.56". */
  toString(): string {
    const sign = this.cents < 0 ? '-' : '';
    const abs = Math.abs(this.cents);
    const whole = Math.trunc(abs / 100);
    const frac = (abs % 100).toString().padStart(2, '0');
    return `${sign}${whole}.${frac}`;
  }

  toJSON(): string {
    return this.toString();
  }
}
