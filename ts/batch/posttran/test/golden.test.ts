import { mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { describe, expect, it } from 'vitest';
import { runCbtrn02c } from '../src/cbtrn02c/index.js';
import { flatFileBindings, main, resolveDdBindings } from '../src/posttran.js';
import { FIXED_CLOCK } from './helpers.js';

/**
 * Golden files in test/golden/ were produced by compiling and running the
 * original app/cbl/CBTRN02C.cbl under GnuCOBOL against app/data/ASCII
 * (tools/gnucobol/run-cobol-posttran.sh). GnuCOBOL line-sequential output
 * drops trailing spaces, so lines are compared right-trimmed. TRAN-PROC-TS
 * (TRAN-RECORD cols 305-330) is wall-clock time in both runs and is checked
 * for format only.
 */
const GOLDEN = fileURLToPath(new URL('./golden/', import.meta.url));
const PROC_TS_START = 304;
const PROC_TS_END = 330;

const read = (path: string): string[] =>
  readFileSync(path, 'utf8')
    .split('\n')
    .map((l) => l.trimEnd())
    .filter((l, i, all) => l.length > 0 || i < all.length - 1);

const nonEmpty = (lines: string[]): string[] => lines.filter((l) => l.length > 0);

describe('golden: POSTTRAN on app/data/ASCII matches GnuCOBOL CBTRN02C', async () => {
  const outDir = mkdtempSync(join(tmpdir(), 'posttran-golden-'));
  const dd = resolveDdBindings({ outDir });
  const sysout: string[] = [];
  const result = await runCbtrn02c(flatFileBindings(dd), { clock: FIXED_CLOCK, display: (l) => sysout.push(l) });

  it('return code and counts', () => {
    expect(String(result.returnCode)).toBe(readFileSync(join(GOLDEN, 'returncode.txt'), 'utf8').trim());
    expect(result).toEqual({ returnCode: 4, transactionCount: 300, rejectCount: 38 });
  });

  it('SYSOUT (DISPLAY output)', () => {
    expect(sysout).toEqual(nonEmpty(read(join(GOLDEN, 'sysout.txt'))));
  });

  it.each(['dalyrejs.txt', 'acctdata.txt', 'tcatbal.txt'])('%s', (file) => {
    expect(nonEmpty(read(join(outDir, file)))).toEqual(nonEmpty(read(join(GOLDEN, file))));
  });

  it('transact.txt (TRAN-PROC-TS masked)', () => {
    const mask = (l: string) => l.slice(0, PROC_TS_START) + l.slice(PROC_TS_END);
    const actual = nonEmpty(read(join(outDir, 'transact.txt')));
    const expected = nonEmpty(read(join(GOLDEN, 'transact.txt')));
    expect(actual.map(mask)).toEqual(expected.map(mask));
    for (const line of actual) {
      expect(line.slice(PROC_TS_START, PROC_TS_END)).toMatch(/^\d{4}-\d{2}-\d{2}-\d{2}\.\d{2}\.\d{2}\.\d{2}0000$/);
    }
    expect(actual[0]?.slice(PROC_TS_START, PROC_TS_END)).toBe('2022-06-11-00.00.00.340000');
  });
});

describe('CLI (npm run posttran equivalent)', () => {
  it('runs STEP15 end-to-end with DD overrides and returns RC 4', async () => {
    const outDir = mkdtempSync(join(tmpdir(), 'posttran-cli-'));
    const rc = await main(['--out-dir', outDir, '--dd', `DALYREJS=${join(outDir, 'rejects.json')}`], {});
    expect(rc).toBe(4);
    const rejects = JSON.parse(readFileSync(join(outDir, 'rejects.json'), 'utf8')) as { validationFailReason: string }[];
    expect(rejects).toHaveLength(38);
    expect(rejects.every((r) => r.validationFailReason === '0102')).toBe(true);
  });

  it('TRANFILE is opened OUTPUT: an existing transact file is replaced, not loaded', async () => {
    const outDir = mkdtempSync(join(tmpdir(), 'posttran-cli-'));
    const golden = readFileSync(join(GOLDEN, 'transact.txt'), 'utf8');
    writeFileSync(join(outDir, 'transact.txt'), golden.split('\n')[0] + '\n');
    const rc = await main(['--out-dir', outDir], {});
    expect(rc).toBe(4);
    expect(nonEmpty(read(join(outDir, 'transact.txt')))).toHaveLength(262);
  });

  it('leaves existing outputs untouched when the job abends', async () => {
    const outDir = mkdtempSync(join(tmpdir(), 'posttran-cli-'));
    writeFileSync(join(outDir, 'dalyrejs.txt'), 'YESTERDAY\n');
    const rc = await main(['--out-dir', outDir], { DD_ACCTFILE: join(outDir, 'missing.txt') });
    expect(rc).toBe(12);
    expect(readFileSync(join(outDir, 'dalyrejs.txt'), 'utf8')).toBe('YESTERDAY\n');
  });

  it('returns 12 (abend) when a required input DD is missing', async () => {
    const outDir = mkdtempSync(join(tmpdir(), 'posttran-cli-'));
    const rc = await main(['--out-dir', outDir], { DD_XREFFILE: join(outDir, 'missing.txt') });
    expect(rc).toBe(12);
  });
});
