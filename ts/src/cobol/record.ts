/**
 * Fixed-length record codec reproducing the on-disk layout of the CardDemo
 * sequential/VSAM datasets (see app/cpy/*.cpy and the RECORDSIZE clauses in
 * app/jcl/*.jcl).
 *
 * Display numeric fields use zoned decimal: unsigned fields are plain digits,
 * signed fields carry the sign as an overpunch on the trailing digit, exactly as
 * in app/data/ASCII/*.txt (e.g. "00000001940{" is +194.00 for PIC S9(10)V99).
 */

import { packedLength, type PictureSpec } from './picture.js';

export interface FieldSpec {
  /** camelCase TypeScript property name. */
  readonly name: string;
  /** Original COBOL data name, e.g. ACCT-CURR-BAL. */
  readonly cobolName: string;
  /** Zero-based byte offset within the record. */
  readonly offset: number;
  readonly length: number;
  readonly picture: PictureSpec;
  readonly filler: boolean;
}

export interface RecordSpec<T extends object> {
  /** TypeScript interface name. */
  readonly name: string;
  /** Source copybook path, relative to the repository root. */
  readonly copybook: string;
  readonly recordLength: number;
  readonly fields: readonly FieldSpec[];
  /** Phantom marker so the decoded shape is tied to the spec. */
  readonly __record?: T;
}

const POSITIVE_OVERPUNCH = '{ABCDEFGHI';
const NEGATIVE_OVERPUNCH = '}JKLMNOPQR';

function decodeZoned(raw: string, picture: PictureSpec): number {
  const text = raw.replace(/\0/g, ' ').trim();
  if (text === '' || /^[* ]+$/.test(text)) return 0;

  let digits = text;
  let negative = false;

  const last = digits[digits.length - 1] as string;
  const positiveIndex = POSITIVE_OVERPUNCH.indexOf(last);
  const negativeIndex = NEGATIVE_OVERPUNCH.indexOf(last);
  if (positiveIndex >= 0) {
    digits = digits.slice(0, -1) + String(positiveIndex);
  } else if (negativeIndex >= 0) {
    negative = true;
    digits = digits.slice(0, -1) + String(negativeIndex);
  } else if (last === '-' || last === '+') {
    negative = last === '-';
    digits = digits.slice(0, -1);
  } else if (digits.startsWith('-')) {
    negative = true;
    digits = digits.slice(1);
  }

  digits = digits.replace(/[^0-9]/g, '');
  if (digits === '') return 0;

  const value = Number(digits) / 10 ** picture.scale;
  return negative ? -value : value;
}

function encodeZoned(value: number, picture: PictureSpec): string {
  const negative = value < 0;
  const scaled = Math.round(Math.abs(value) * 10 ** picture.scale);
  let digits = String(scaled).padStart(picture.digits, '0');
  if (digits.length > picture.digits) {
    digits = digits.slice(digits.length - picture.digits);
  }
  if (!picture.signed) return digits;

  const lastDigit = Number(digits[digits.length - 1]);
  const overpunch = (negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH)[lastDigit] as string;
  return digits.slice(0, -1) + overpunch;
}

function decodePacked(bytes: Buffer, picture: PictureSpec): number {
  let digits = '';
  for (let i = 0; i < bytes.length; i += 1) {
    const byte = bytes[i] as number;
    const high = (byte >> 4) & 0x0f;
    const low = byte & 0x0f;
    digits += String(high);
    if (i < bytes.length - 1) digits += String(low);
  }
  const signNibble = (bytes[bytes.length - 1] as number) & 0x0f;
  const negative = signNibble === 0x0d || signNibble === 0x0b;
  const value = Number(digits.slice(-picture.digits)) / 10 ** picture.scale;
  return negative ? -value : value;
}

function encodePacked(value: number, picture: PictureSpec): Buffer {
  const negative = value < 0;
  const scaled = Math.round(Math.abs(value) * 10 ** picture.scale);
  const digits = String(scaled).padStart(picture.digits, '0').slice(-picture.digits);
  const padded = digits.length % 2 === 0 ? `0${digits}` : digits;
  const bytes = Buffer.alloc(packedLength(picture.digits));
  for (let i = 0; i < padded.length; i += 2) {
    const high = Number(padded[i]);
    const low = i + 1 < padded.length ? Number(padded[i + 1]) : 0;
    bytes[i / 2] = (high << 4) | low;
  }
  const signNibble = negative ? 0x0d : 0x0c;
  const lastIndex = bytes.length - 1;
  bytes[lastIndex] = ((bytes[lastIndex] as number) & 0xf0) | signNibble;
  return bytes;
}

/** Decodes one fixed-length record into a plain object keyed by field name. */
export function decodeRecord<T extends object>(spec: RecordSpec<T>, raw: string | Buffer): T {
  const buffer = Buffer.isBuffer(raw) ? raw : Buffer.from(raw, 'latin1');
  const values: Record<string, string | number> = {};

  for (const field of spec.fields) {
    if (field.filler) continue;
    const slice = buffer.subarray(field.offset, field.offset + field.length);
    if (field.picture.kind === 'numeric') {
      values[field.name] = field.picture.packed
        ? decodePacked(slice, field.picture)
        : decodeZoned(slice.toString('latin1'), field.picture);
    } else {
      values[field.name] = slice.toString('latin1').replace(/\0/g, ' ').trimEnd();
    }
  }

  return values as T;
}

/** Encodes a record back into its fixed-length representation, space padded. */
export function encodeRecord<T extends object>(spec: RecordSpec<T>, record: T): string {
  const buffer = Buffer.alloc(spec.recordLength, ' ');
  const values = record as Record<string, string | number | undefined>;

  for (const field of spec.fields) {
    if (field.filler) continue;
    const value = values[field.name];
    if (value === undefined) continue;

    if (field.picture.kind === 'numeric') {
      const numeric = typeof value === 'number' ? value : Number(value);
      if (field.picture.packed) {
        encodePacked(numeric, field.picture).copy(buffer, field.offset);
      } else {
        buffer.write(encodeZoned(numeric, field.picture), field.offset, 'latin1');
      }
    } else {
      const text = String(value).slice(0, field.length).padEnd(field.length, ' ');
      buffer.write(text, field.offset, 'latin1');
    }
  }

  return buffer.toString('latin1');
}

/** Splits an unblocked fixed-length-record file (no line terminators) into records. */
export function splitFixedLengthRecords(content: Buffer, recordLength: number): Buffer[] {
  const records: Buffer[] = [];

  for (let offset = 0; offset + recordLength <= content.length; offset += recordLength) {
    const record = content.subarray(offset, offset + recordLength);
    if (record.every((byte) => byte === 0x20)) continue;
    records.push(record);
  }

  return records;
}

/**
 * Splits a newline-delimited text rendering of a fixed-length file (the
 * app/data/ASCII/*.txt files), padding lines whose trailing spaces were
 * stripped back to the record length.
 */
export function splitTextRecords(content: Buffer, recordLength: number): Buffer[] {
  return content
    .toString('latin1')
    .split('\n')
    .map((line) => line.replace(/\r$/, ''))
    .filter((line) => line.trim() !== '')
    .map((line) => Buffer.from(line.slice(0, recordLength).padEnd(recordLength, ' '), 'latin1'));
}

/** Extracts the raw key bytes of a record, mirroring VSAM KEYS(length offset). */
export function recordKey(raw: string | Buffer, keyLength: number, keyOffset = 0): string {
  const buffer = Buffer.isBuffer(raw) ? raw : Buffer.from(raw, 'latin1');
  return buffer.subarray(keyOffset, keyOffset + keyLength).toString('latin1');
}
