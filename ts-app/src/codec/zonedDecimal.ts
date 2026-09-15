import { Decimal } from 'decimal.js';

/**
 * Zoned decimal (DISPLAY usage) encoding used by the CardDemo flat files.
 *
 * A `PIC S9(n)V99` field is stored as n+2 characters where the last character
 * carries both the low-order digit and the sign as an "overpunch":
 *   +0..+9 -> { A B C D E F G H I
 *   -0..-9 -> } J K L M N O P Q R
 */
const POSITIVE_OVERPUNCH = '{ABCDEFGHI';
const NEGATIVE_OVERPUNCH = '}JKLMNOPQR';

export function decodeZoned(raw: string, scale: number): Decimal {
  const text = raw.trim();
  if (text === '') return new Decimal(0);

  const last = text[text.length - 1] as string;
  let digits = text.slice(0, -1);
  let negative = false;

  const positiveIndex = POSITIVE_OVERPUNCH.indexOf(last);
  const negativeIndex = NEGATIVE_OVERPUNCH.indexOf(last);
  if (positiveIndex >= 0) {
    digits += String(positiveIndex);
  } else if (negativeIndex >= 0) {
    digits += String(negativeIndex);
    negative = true;
  } else if (last === '-') {
    negative = true;
  } else if (last === '+') {
    // sign only, nothing to append
  } else {
    digits += last;
  }

  const normalized = digits.replace(/\D/g, '') || '0';
  const value = new Decimal(normalized).dividedBy(new Decimal(10).pow(scale));
  return negative ? value.negated() : value;
}

export function encodeZoned(value: Decimal, length: number, scale: number): string {
  const scaled = value.times(new Decimal(10).pow(scale)).toDecimalPlaces(0, Decimal.ROUND_DOWN);
  const negative = scaled.isNegative();
  const digits = scaled.abs().toFixed(0).padStart(length, '0').slice(-length);
  const lowOrder = Number(digits[digits.length - 1]);
  const overpunch = negative ? NEGATIVE_OVERPUNCH[lowOrder] : POSITIVE_OVERPUNCH[lowOrder];
  return digits.slice(0, -1) + overpunch;
}
