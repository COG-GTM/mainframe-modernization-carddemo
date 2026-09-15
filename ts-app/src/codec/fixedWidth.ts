import type { Decimal } from 'decimal.js';
import { decodeZoned, encodeZoned } from './zonedDecimal.js';

/**
 * Declarative description of a COBOL copybook record so a single codec can
 * read and write every fixed-width file in `app/data/`.
 *
 *   PIC X(n)        -> { kind: 'alnum' }
 *   PIC 9(n)        -> { kind: 'digits' }   (kept as a string, leading zeros preserved)
 *   PIC S9(n)V9(s)  -> { kind: 'signed', scale: s }
 *   FILLER          -> { kind: 'filler' }
 */
export type FieldSpec =
  | { name: string; kind: 'alnum'; length: number }
  | { name: string; kind: 'digits'; length: number }
  | { name: string; kind: 'signed'; length: number; scale: number }
  | { name: '__filler'; kind: 'filler'; length: number };

export type RecordLayout<T> = {
  /** COBOL copybook this layout was translated from, e.g. `CVACT01Y`. */
  readonly copybook: string;
  /** COBOL record length (RECLN). */
  readonly recordLength: number;
  readonly fields: readonly FieldSpec[];
  /** Phantom marker so a layout is bound to the record type it decodes. */
  readonly __record?: T;
};

export function layoutLength(fields: readonly FieldSpec[]): number {
  return fields.reduce((total, field) => total + field.length, 0);
}

export function alnum(name: string, length: number): FieldSpec {
  return { name, kind: 'alnum', length };
}

export function digits(name: string, length: number): FieldSpec {
  return { name, kind: 'digits', length };
}

export function signed(name: string, length: number, scale: number): FieldSpec {
  return { name, kind: 'signed', length, scale };
}

export function filler(length: number): FieldSpec {
  return { name: '__filler', kind: 'filler', length };
}

type DecodedValue = string | Decimal;

/**
 * Decodes one fixed-width line. Lines shorter than the record length are
 * space-padded: several sample files in `app/data/ASCII` drop trailing FILLER.
 */
export function decodeRecord<T>(layout: RecordLayout<T>, line: string): T {
  const padded = line.padEnd(layoutLength(layout.fields), ' ');
  const out: Record<string, DecodedValue> = {};
  let offset = 0;

  for (const field of layout.fields) {
    const raw = padded.slice(offset, offset + field.length);
    offset += field.length;
    switch (field.kind) {
      case 'filler':
        break;
      case 'alnum':
        out[field.name] = raw.trimEnd();
        break;
      case 'digits':
        out[field.name] = raw.trim() === '' ? '0'.repeat(field.length) : raw;
        break;
      case 'signed':
        out[field.name] = decodeZoned(raw, field.scale);
        break;
    }
  }

  return out as T;
}

export function encodeRecord<T extends object>(layout: RecordLayout<T>, record: T): string {
  const source = record as Record<string, unknown>;
  let out = '';

  for (const field of layout.fields) {
    switch (field.kind) {
      case 'filler':
        out += ' '.repeat(field.length);
        break;
      case 'alnum':
        out += String(source[field.name] ?? '')
          .padEnd(field.length, ' ')
          .slice(0, field.length);
        break;
      case 'digits':
        out += String(source[field.name] ?? '')
          .trim()
          .padStart(field.length, '0')
          .slice(-field.length);
        break;
      case 'signed':
        out += encodeZoned(source[field.name] as Decimal, field.length, field.scale);
        break;
    }
  }

  return out;
}

export function decodeLines<T>(layout: RecordLayout<T>, contents: string): T[] {
  return contents
    .split(/\r?\n/)
    .filter((line) => line.trim() !== '')
    .map((line) => decodeRecord(layout, line));
}
