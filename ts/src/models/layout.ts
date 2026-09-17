/**
 * Fixed-length record layout primitives.
 *
 * COBOL records in CardDemo are fixed-length byte strings whose fields are
 * described by PIC clauses. Every model in this folder declares a `RecordLayout`
 * mirroring the copybook so that records can be decoded from, and encoded back
 * to, the exact byte layout used by the VSAM/QSAM datasets.
 *
 * Supported PIC forms in this application:
 *   PIC X(n)        -> alphanumeric, space padded on the right
 *   PIC 9(n)        -> unsigned zoned decimal, zero padded on the left
 *   PIC S9(n)V99    -> signed zoned decimal, sign carried as an overpunch on
 *                      the last digit, implied decimal point (no stored '.')
 */

export type FieldSpec =
  | { readonly kind: 'alphanumeric'; readonly name: string; readonly length: number }
  | { readonly kind: 'filler'; readonly name?: string; readonly length: number }
  | { readonly kind: 'unsigned'; readonly name: string; readonly length: number }
  | {
      readonly kind: 'signed';
      readonly name: string;
      /** Total number of stored digits (integer digits + scale). */
      readonly length: number;
      /** Number of digits after the implied decimal point. */
      readonly scale: number;
    };

export interface RecordLayout<T> {
  /** Copybook the layout was derived from, e.g. `CVACT01Y`. */
  readonly copybook: string;
  /** Record length in bytes (LRECL). */
  readonly length: number;
  readonly fields: readonly FieldSpec[];
  /** Builds a record object from decoded field values. */
  readonly fromFields: (values: Readonly<Record<string, string | number>>) => T;
  /** Extracts the field values to encode from a record object. */
  readonly toFields: (record: T) => Readonly<Record<string, string | number>>;
}

/** Trailing sign overpunch used by zoned decimals: '{' = +0, 'A'..'I' = +1..+9. */
const POSITIVE_OVERPUNCH = '{ABCDEFGHI';
/** '}' = -0, 'J'..'R' = -1..-9. */
const NEGATIVE_OVERPUNCH = '}JKLMNOPQR';

export class RecordFormatError extends Error {}

export function decodeZonedSigned(raw: string, scale: number): number {
  if (raw.length === 0) {
    throw new RecordFormatError('empty zoned decimal field');
  }
  const body = raw.slice(0, -1);
  const last = raw.slice(-1);

  let sign = 1;
  let lastDigit: string;
  const positiveIndex = POSITIVE_OVERPUNCH.indexOf(last);
  const negativeIndex = NEGATIVE_OVERPUNCH.indexOf(last);
  if (positiveIndex >= 0) {
    lastDigit = String(positiveIndex);
  } else if (negativeIndex >= 0) {
    sign = -1;
    lastDigit = String(negativeIndex);
  } else if (last >= '0' && last <= '9') {
    lastDigit = last;
  } else if (last === ' ' && body.trim() === '') {
    return 0;
  } else {
    throw new RecordFormatError(`invalid zoned decimal value: ${JSON.stringify(raw)}`);
  }

  const digits = (body.replace(/ /g, '0') + lastDigit).replace(/[^0-9]/g, '0');
  const unscaled = Number(digits);
  if (!Number.isFinite(unscaled)) {
    throw new RecordFormatError(`invalid zoned decimal value: ${JSON.stringify(raw)}`);
  }
  return (sign * unscaled) / 10 ** scale;
}

export function encodeZonedSigned(value: number, length: number, scale: number): string {
  const unscaled = Math.round(Math.abs(value) * 10 ** scale);
  const digits = String(unscaled).padStart(length, '0');
  if (digits.length > length) {
    throw new RecordFormatError(`value ${value} does not fit in S9(${length - scale})V9(${scale})`);
  }
  const lastDigit = Number(digits.slice(-1));
  const overpunch = value < 0 ? NEGATIVE_OVERPUNCH[lastDigit] : POSITIVE_OVERPUNCH[lastDigit];
  return digits.slice(0, -1) + overpunch;
}

export function decodeUnsigned(raw: string): number {
  const trimmed = raw.trim();
  if (trimmed === '') {
    return 0;
  }
  const digits = trimmed.replace(/[^0-9]/g, '');
  const value = Number(digits === '' ? '0' : digits);
  if (!Number.isFinite(value)) {
    throw new RecordFormatError(`invalid numeric value: ${JSON.stringify(raw)}`);
  }
  return value;
}

export function encodeUnsigned(value: number, length: number): string {
  const digits = String(Math.abs(Math.round(value))).padStart(length, '0');
  if (digits.length > length) {
    throw new RecordFormatError(`value ${value} does not fit in 9(${length})`);
  }
  return digits;
}

export function encodeAlphanumeric(value: string, length: number): string {
  return value.length >= length ? value.slice(0, length) : value.padEnd(length, ' ');
}

/** Sum of the field lengths, i.e. the LRECL implied by the layout. */
export function layoutLength(fields: readonly FieldSpec[]): number {
  return fields.reduce((total, field) => total + field.length, 0);
}

export function decodeRecord<T>(layout: RecordLayout<T>, raw: string): T {
  const padded = raw.length < layout.length ? raw.padEnd(layout.length, ' ') : raw;
  const values: Record<string, string | number> = {};
  let offset = 0;
  for (const field of layout.fields) {
    const slice = padded.slice(offset, offset + field.length);
    offset += field.length;
    switch (field.kind) {
      case 'filler':
        break;
      case 'alphanumeric':
        values[field.name] = slice;
        break;
      case 'unsigned':
        values[field.name] = decodeUnsigned(slice);
        break;
      case 'signed':
        values[field.name] = decodeZonedSigned(slice, field.scale);
        break;
    }
  }
  return layout.fromFields(values);
}

export function encodeRecord<T>(layout: RecordLayout<T>, record: T): string {
  const values = layout.toFields(record);
  let out = '';
  for (const field of layout.fields) {
    switch (field.kind) {
      case 'filler':
        out += ' '.repeat(field.length);
        break;
      case 'alphanumeric':
        out += encodeAlphanumeric(String(values[field.name] ?? ''), field.length);
        break;
      case 'unsigned':
        out += encodeUnsigned(Number(values[field.name] ?? 0), field.length);
        break;
      case 'signed':
        out += encodeZonedSigned(Number(values[field.name] ?? 0), field.length, field.scale);
        break;
    }
  }
  return out;
}

/** Right-trims an alphanumeric field the way COBOL programs treat trailing spaces. */
export function trimField(value: string): string {
  return value.replace(/\s+$/, '');
}
