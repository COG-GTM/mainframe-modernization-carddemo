import { Decimal } from 'decimal.js';

/**
 * COBOL numeric semantics on top of decimal.js.
 *
 * All monetary fields are held as `Decimal` (never JS `number`) so that
 * PIC S9(n)V99 values keep exact cents, and stores into a PIC truncate the
 * same way an IBM COBOL MOVE/ADD/COMPUTE without ON SIZE ERROR does.
 */
export const CobolDecimal = Decimal.clone({ precision: 64, rounding: Decimal.ROUND_DOWN });
export type CobolDecimal = Decimal;

export function dec(value: Decimal.Value): Decimal {
  return new CobolDecimal(value);
}

/**
 * Store `value` into a PIC S9(intDigits)V9(scale) receiving field:
 * excess fractional digits are truncated and high-order integer digits are
 * silently dropped (COBOL size-error behaviour when ON SIZE ERROR is absent).
 */
export function toPic(value: Decimal, intDigits: number, scale: number): Decimal {
  const truncated = value.toDecimalPlaces(scale, Decimal.ROUND_DOWN);
  const limit = new CobolDecimal(10).pow(intDigits);
  // decimal.js modulo uses ROUND_DOWN mode by default => sign follows dividend.
  const result = truncated.mod(limit);
  return result.isZero() ? new CobolDecimal(0) : result;
}

// ---------------------------------------------------------------------------
// DISPLAY zoned decimal (PIC S9(n)V99) with trailing sign overpunch, as found
// in the ASCII sample data under app/data/ASCII (EBCDIC convention mapped to
// ASCII): '{' A-I => +0..+9, '}' J-R => -0..-9.
// ---------------------------------------------------------------------------
const POSITIVE_OVERPUNCH = '{ABCDEFGHI';
const NEGATIVE_OVERPUNCH = '}JKLMNOPQR';

export class NumericDataError extends Error {}

export function decodeZoned(raw: string, intDigits: number, scale: number): Decimal {
  const length = intDigits + scale;
  if (raw.length !== length) {
    throw new NumericDataError(`zoned field length ${raw.length} != ${length}: "${raw}"`);
  }
  if (raw.trim() === '') return new CobolDecimal(0);
  const body = raw.slice(0, -1);
  const last = raw.charAt(length - 1);
  let negative = false;
  let lastDigit: number;
  if (last >= '0' && last <= '9') {
    lastDigit = last.charCodeAt(0) - 48;
  } else if (POSITIVE_OVERPUNCH.includes(last)) {
    lastDigit = POSITIVE_OVERPUNCH.indexOf(last);
  } else if (NEGATIVE_OVERPUNCH.includes(last)) {
    lastDigit = NEGATIVE_OVERPUNCH.indexOf(last);
    negative = true;
  } else {
    throw new NumericDataError(`invalid zoned sign character "${last}" in "${raw}"`);
  }
  if (!/^[0-9]*$/.test(body)) {
    throw new NumericDataError(`non-numeric zoned field "${raw}"`);
  }
  const digits = body + String(lastDigit);
  const unscaled = new CobolDecimal(digits);
  const value = unscaled.div(new CobolDecimal(10).pow(scale));
  return negative && !value.isZero() ? value.neg() : value;
}

export function encodeZoned(value: Decimal, intDigits: number, scale: number): string {
  const stored = toPic(value, intDigits, scale);
  const negative = stored.isNegative() && !stored.isZero();
  const unscaled = stored.abs().mul(new CobolDecimal(10).pow(scale)).toFixed(0);
  const digits = unscaled.padStart(intDigits + scale, '0');
  const lastDigit = digits.charCodeAt(digits.length - 1) - 48;
  const sign = (negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH).charAt(lastDigit);
  return digits.slice(0, -1) + sign;
}

// ---------------------------------------------------------------------------
// COMP-3 packed decimal. Not used by the ASCII sample data or by RecordLayout
// (which is text/zoned only). These are building blocks for a future byte-oriented
// reader of EBCDIC/binary extracts (app/data/EBCDIC); no such reader exists yet.
// ---------------------------------------------------------------------------
export function packedLength(totalDigits: number): number {
  return Math.floor(totalDigits / 2) + 1;
}

export function decodePacked(bytes: Uint8Array, scale: number): Decimal {
  if (bytes.length === 0) throw new NumericDataError('empty packed field');
  let digits = '';
  for (let i = 0; i < bytes.length; i++) {
    const b = bytes[i] as number;
    const hi = b >> 4;
    const lo = b & 0x0f;
    if (hi > 9) throw new NumericDataError(`invalid packed digit nibble 0x${hi.toString(16)}`);
    digits += String(hi);
    if (i < bytes.length - 1) {
      if (lo > 9) throw new NumericDataError(`invalid packed digit nibble 0x${lo.toString(16)}`);
      digits += String(lo);
    } else if (lo < 0x0a) {
      throw new NumericDataError(`invalid packed sign nibble 0x${lo.toString(16)}`);
    }
  }
  const signNibble = (bytes[bytes.length - 1] as number) & 0x0f;
  const negative = signNibble === 0x0d || signNibble === 0x0b;
  const value = new CobolDecimal(digits).div(new CobolDecimal(10).pow(scale));
  return negative && !value.isZero() ? value.neg() : value;
}

export function encodePacked(value: Decimal, intDigits: number, scale: number): Uint8Array {
  const totalDigits = intDigits + scale;
  const stored = toPic(value, intDigits, scale);
  const negative = stored.isNegative() && !stored.isZero();
  const length = packedLength(totalDigits);
  const digits = stored.abs().mul(new CobolDecimal(10).pow(scale)).toFixed(0).padStart(length * 2 - 1, '0');
  const nibbles = [...digits].map((d) => d.charCodeAt(0) - 48);
  nibbles.push(negative ? 0x0d : 0x0c);
  const out = new Uint8Array(length);
  for (let i = 0; i < length; i++) {
    out[i] = ((nibbles[i * 2] as number) << 4) | (nibbles[i * 2 + 1] as number);
  }
  return out;
}

// ---------------------------------------------------------------------------
// Alphanumeric helpers
// ---------------------------------------------------------------------------

/** COBOL alphanumeric comparison: the shorter operand is padded with spaces. */
export function compareAlphanumeric(a: string, b: string): number {
  const len = Math.max(a.length, b.length);
  const pa = a.padEnd(len, ' ');
  const pb = b.padEnd(len, ' ');
  return pa < pb ? -1 : pa > pb ? 1 : 0;
}
