import { readFileSync, writeFileSync } from 'node:fs';
import { decodeRecord, encodeRecord, RecordLayout } from '../models/layout';

/**
 * Reader/writer for the fixed-length (RECFM=FB) sequential datasets. The ASCII
 * sample files under `app/data/ASCII` store one LRECL-sized record per line;
 * EBCDIC-converted dumps may have no line separators at all, so both shapes are
 * accepted.
 */

export function splitFixedWidth(content: string, recordLength: number): string[] {
  const normalised = content.replace(/\r\n/g, '\n');
  if (normalised.includes('\n')) {
    return normalised
      .split('\n')
      .filter((line) => line.trim() !== '')
      .map((line) => (line.length < recordLength ? line.padEnd(recordLength, ' ') : line));
  }
  const records: string[] = [];
  for (let offset = 0; offset + recordLength <= normalised.length; offset += recordLength) {
    records.push(normalised.slice(offset, offset + recordLength));
  }
  return records;
}

export function readFixedWidthFile<T>(path: string, layout: RecordLayout<T>): T[] {
  const content = readFileSync(path, 'latin1');
  return splitFixedWidth(content, layout.length).map((raw) => decodeRecord(layout, raw));
}

export function writeFixedWidthFile<T>(path: string, layout: RecordLayout<T>, records: T[]): void {
  const lines = records.map((record) => encodeRecord(layout, record));
  writeFileSync(path, lines.length === 0 ? '' : `${lines.join('\n')}\n`, 'latin1');
}

/** Writes a report/print dataset: one already formatted line per record. */
export function writeLineFile(path: string, lines: readonly string[]): void {
  writeFileSync(path, lines.length === 0 ? '' : `${lines.join('\n')}\n`, 'latin1');
}
