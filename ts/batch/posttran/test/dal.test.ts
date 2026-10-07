import { mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import { CARD_XREF_LAYOUT, type CardXrefRecord, cardXrefKey } from '../src/copybooks/index.js';
import { FlatFileKeyedFile, FlatFileSequentialInput, FlatFileSequentialOutput, InMemoryKeyedFile } from '../src/dal/index.js';

const xref = (card: string, acct: string): CardXrefRecord => ({ xrefCardNum: card, xrefCustId: '000000001', xrefAcctId: acct });

describe('InMemoryKeyedFile (VSAM KSDS semantics)', () => {
  it('returns 23 / 22 / 23 for not-found read, duplicate write, missing rewrite', async () => {
    const f = new InMemoryKeyedFile(cardXrefKey, [xref('1111222233334444', '00000000001')]);
    expect(await f.open()).toBe('00');
    expect((await f.read('9999999999999999')).status).toBe('23');
    expect((await f.read('1111222233334444')).record?.xrefAcctId).toBe('00000000001');
    expect(await f.write(xref('1111222233334444', '00000000002'))).toBe('22');
    expect(await f.rewrite(xref('5555666677778888', '00000000002'))).toBe('23');
    expect(await f.rewrite(xref('1111222233334444', '00000000009'))).toBe('00');
    expect((await f.read('1111222233334444')).record?.xrefAcctId).toBe('00000000009');
  });

  it('returns 48 when not open', async () => {
    const f = new InMemoryKeyedFile(cardXrefKey);
    expect((await f.read('x')).status).toBe('48');
  });
});

describe('flat-file implementations', () => {
  const dir = mkdtempSync(join(tmpdir(), 'posttran-dal-'));

  it('returns 35 when an input dataset is missing', async () => {
    expect(await new FlatFileSequentialInput(join(dir, 'nope.txt'), CARD_XREF_LAYOUT).open()).toBe('35');
    expect(await new FlatFileKeyedFile(join(dir, 'nope.txt'), CARD_XREF_LAYOUT, cardXrefKey).open()).toBe('35');
  });

  it('creates an empty KSDS when createIfMissing is set and writes it in key order', async () => {
    const out = join(dir, 'xref-out.txt');
    const f = new FlatFileKeyedFile(join(dir, 'missing.txt'), CARD_XREF_LAYOUT, cardXrefKey, {
      createIfMissing: true,
      outputPath: out,
    });
    expect(await f.open()).toBe('00');
    await f.write(xref('9000000000000000', '00000000002'));
    await f.write(xref('1000000000000000', '00000000001'));
    expect(await f.close()).toBe('00');
    const text = readFileSync(out, 'utf8').split('\n');
    expect(text[0]?.startsWith('1000000000000000')).toBe(true);
    expect(text[1]?.startsWith('9000000000000000')).toBe(true);
  });

  it('reads sequential fixed-width input to EOF (status 10)', async () => {
    const p = join(dir, 'seq.txt');
    writeFileSync(p, '111122223333444400000000100000000001\n');
    const f = new FlatFileSequentialInput(p, CARD_XREF_LAYOUT);
    expect(await f.open()).toBe('00');
    expect((await f.read()).record?.xrefAcctId).toBe('00000000001');
    expect((await f.read()).status).toBe('10');
  });

  it('writes JSON when the path ends in .json', async () => {
    const p = join(dir, 'out.json');
    const f = new FlatFileSequentialOutput(p, CARD_XREF_LAYOUT);
    await f.open();
    await f.write(xref('1111222233334444', '00000000001'));
    await f.close();
    expect(JSON.parse(readFileSync(p, 'utf8'))).toEqual([
      { xrefCardNum: '1111222233334444', xrefCustId: '000000001', xrefAcctId: '00000000001' },
    ]);
  });
});
