import type { Decimal } from 'decimal.js';
import { CobolDecimal, decodeZoned, encodeZoned } from './numeric.js';

/**
 * Declarative fixed-width record layout, one entry per COBOL elementary item.
 *
 * - `alpha`    PIC X(n)          -> string (trailing spaces trimmed on read,
 *                                   space-padded / truncated on write)
 * - `unsigned` PIC 9(n)          -> string of n digits (keeps leading zeros,
 *                                   safe for keys such as ACCT-ID 9(11))
 * - `signed`   PIC S9(i)V9(s)    -> Decimal (zoned, trailing overpunch)
 * - `filler`   FILLER PIC X(n)   -> not exposed as a property; the raw bytes
 *                                   read are kept under the `FILLERS` symbol so a
 *                                   READ ... REWRITE round-trip preserves them
 *                                   (records built from scratch get spaces)
 */
export const FILLERS: unique symbol = Symbol('cobol.fillers');

interface WithFillers {
  [FILLERS]?: string[];
}
export type FieldDef<T> =
  | { kind: 'alpha'; name: keyof T & string; length: number }
  | { kind: 'unsigned'; name: keyof T & string; length: number }
  | { kind: 'signed'; name: keyof T & string; intDigits: number; scale: number }
  | { kind: 'filler'; length: number };

export interface RecordLayout<T> {
  /** Copybook / 01-level name, e.g. "DALYTRAN-RECORD (CVTRA06Y)". */
  readonly name: string;
  readonly length: number;
  readonly fields: readonly FieldDef<T>[];
  parse(line: string): T;
  format(record: T): string;
  toJSON(record: T): Record<string, string>;
  fromJSON(obj: Record<string, unknown>): T;
  empty(): T;
  initialize(record: T): T;
}

export class RecordFormatError extends Error {}

function fieldLength<T>(f: FieldDef<T>): number {
  return f.kind === 'signed' ? f.intDigits + f.scale : f.length;
}

export function defineLayout<T>(name: string, fields: readonly FieldDef<T>[]): RecordLayout<T> {
  const length = fields.reduce((n, f) => n + fieldLength(f), 0);

  const formatUnsigned = (value: string, len: number, fieldName: string): string => {
    const v = String(value ?? '');
    if (!/^[0-9]*$/.test(v)) {
      throw new RecordFormatError(`${name}.${fieldName}: "${v}" is not numeric`);
    }
    // MOVE to a shorter PIC 9 field truncates high-order digits.
    return v.padStart(len, '0').slice(-len);
  };

  return {
    name,
    length,
    fields,

    parse(line: string): T {
      const raw = line.replace(/\r?\n$/, '');
      if (raw.length > length) {
        throw new RecordFormatError(`${name}: record length ${raw.length} exceeds ${length}`);
      }
      const padded = raw.padEnd(length, ' ');
      const out: Record<string, unknown> = {};
      const fillers: string[] = [];
      let pos = 0;
      for (const f of fields) {
        const len = fieldLength(f);
        const slice = padded.slice(pos, pos + len);
        pos += len;
        switch (f.kind) {
          case 'alpha':
            out[f.name] = slice.trimEnd();
            break;
          case 'unsigned':
            // All-blank is treated as zero; partially blank digits are invalid PIC 9 data.
            if (slice.trim() === '') {
              out[f.name] = '0'.repeat(len);
            } else if (/^[0-9]+$/.test(slice)) {
              out[f.name] = slice;
            } else {
              throw new RecordFormatError(`${name}.${f.name}: "${slice}" is not numeric`);
            }
            break;
          case 'signed':
            out[f.name] = decodeZoned(slice, f.intDigits, f.scale);
            break;
          case 'filler':
            fillers.push(slice);
            break;
        }
      }
      if (fillers.length > 0) (out as WithFillers)[FILLERS] = fillers;
      return out as T;
    },

    format(record: T): string {
      const rec = record as Record<string, unknown> & WithFillers;
      const fillers = rec[FILLERS] ?? [];
      let fillerIndex = 0;
      let line = '';
      for (const f of fields) {
        switch (f.kind) {
          case 'alpha':
            line += String(rec[f.name] ?? '').padEnd(f.length, ' ').slice(0, f.length);
            break;
          case 'unsigned':
            line += formatUnsigned(rec[f.name] as string, f.length, f.name);
            break;
          case 'signed':
            line += encodeZoned(rec[f.name] as Decimal, f.intDigits, f.scale);
            break;
          case 'filler':
            line += (fillers[fillerIndex++] ?? '').padEnd(f.length, ' ').slice(0, f.length);
            break;
        }
      }
      return line;
    },

    toJSON(record: T): Record<string, string> {
      const rec = record as Record<string, unknown>;
      const out: Record<string, string> = {};
      const fillers = (record as WithFillers)[FILLERS] ?? [];
      let fillerIndex = 0;
      for (const f of fields) {
        if (f.kind === 'filler') {
          const raw = fillers[fillerIndex++];
          // Only non-blank FILLER is emitted, so blank FILLER round-trips to the same spaces.
          if (raw !== undefined && raw.trim() !== '') out[`FILLER-${fillerIndex}`] = raw;
          continue;
        }
        const v = rec[f.name];
        out[f.name] = f.kind === 'signed' ? (v as Decimal).toFixed(f.scale) : String(v ?? '');
      }
      return out;
    },

    fromJSON(obj: Record<string, unknown>): T {
      const out: Record<string, unknown> = {};
      const fillers: string[] = [];
      let fillerIndex = 0;
      for (const f of fields) {
        if (f.kind === 'filler') {
          const raw = obj[`FILLER-${++fillerIndex}`];
          fillers.push(typeof raw === 'string' ? raw.padEnd(f.length, ' ').slice(0, f.length) : ' '.repeat(f.length));
          continue;
        }
        const v = obj[f.name];
        switch (f.kind) {
          case 'alpha':
            out[f.name] = String(v ?? '').trimEnd();
            break;
          case 'unsigned':
            out[f.name] = formatUnsigned(String(v ?? '0'), f.length, f.name);
            break;
          case 'signed':
            out[f.name] = new CobolDecimal(String(v ?? '0'));
            break;
        }
      }
      if (fillers.length > 0) (out as WithFillers)[FILLERS] = fillers;
      return out as T;
    },

    /** A freshly allocated record: spaces for X / FILLER, zeros for 9. */
    empty(): T {
      const out: Record<string, unknown> = {};
      for (const f of fields) {
        if (f.kind === 'alpha') out[f.name] = '';
        else if (f.kind === 'unsigned') out[f.name] = '0'.repeat(f.length);
        else if (f.kind === 'signed') out[f.name] = new CobolDecimal(0);
      }
      return out as T;
    },

    /**
     * COBOL INITIALIZE: named X fields to spaces, 9 fields to zero; FILLER is
     * left untouched (IBM / GnuCOBOL semantics without WITH FILLER).
     */
    initialize(record: T): T {
      const out = this.empty() as Record<string, unknown> & WithFillers;
      const prev = (record as WithFillers)[FILLERS];
      if (prev) out[FILLERS] = [...prev];
      return out as T;
    },
  };
}
