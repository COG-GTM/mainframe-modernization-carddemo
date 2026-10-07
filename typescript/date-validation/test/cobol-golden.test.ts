import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';
import { validateDate, validateDateOfBirth, type DateValidationResult, type FieldFlag } from '../src/index.js';

/**
 * Compares the port against output of the real CSUTLDPY paragraphs compiled
 * with GnuCOBOL (tools/gnucobol/run-cobol-golden.sh). Date-of-birth cases use
 * dates far from today, so they don't depend on when the golden file was made.
 */
const goldenDir = new URL('./golden/', import.meta.url);
const read = (name: string): string[] =>
  readFileSync(new URL(name, goldenDir), 'utf8').split('\n').filter((line) => line.length > 0);

const FLAG_CHAR: Record<FieldFlag, string> = { VALID: 'V', NOT_OK: '0', BLANK: 'B' };

function format(mode: string, date: string, r: DateValidationResult): string {
  const { year, month, day } = r.flags;
  return [mode, date, FLAG_CHAR[year], FLAG_CHAR[month], FLAG_CHAR[day], r.valid ? '0' : '1', r.message]
    .join('|')
    .trimEnd();
}

const cases = read('cases.txt').map((line) => ({ mode: line[0] ?? 'D', date: line.slice(1).padEnd(8, ' ').slice(0, 8) }));
const expected = read('cobol-results.txt');

describe('matches GnuCOBOL output of CSUTLDPY', () => {
  it('has one golden result per case', () => {
    expect(expected).toHaveLength(cases.length);
  });

  it.each(cases.map((c, i) => [`${c.mode} '${c.date}'`, c, expected[i]] as const))('%s', (_name, c, golden) => {
    const result = c.mode === 'B' ? validateDateOfBirth(c.date) : validateDate(c.date);
    expect(format(c.mode, c.date, result)).toBe(golden);
  });
});
