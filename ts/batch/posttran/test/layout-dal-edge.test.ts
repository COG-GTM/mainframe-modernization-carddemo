import { existsSync, mkdtempSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import {
  ACCOUNT_LAYOUT,
  CARD_XREF_LAYOUT,
  type CardXrefRecord,
  DIS_GROUP_LAYOUT,
  type DisGroupRecord,
  TRAN_CAT_BAL_LAYOUT,
  TRAN_CAT_LAYOUT,
  type TranCatRecord,
  cardXrefKey,

  disGroupKey,
  tranCatKey,
} from '../src/copybooks/index.js';
import { RecordFormatError } from '../src/copybooks/layout.js';
import { FlatFileKeyedFile, FlatFileSequentialOutput, readRecords, writeRecords } from '../src/dal/index.js';

const DATA = join(import.meta.dirname, '../../../../app/data/ASCII');
const tmp = () => mkdtempSync(join(tmpdir(), 'posttran-edge-'));

describe('RecordLayout edge cases', () => {
  it('rejects partially blank PIC 9 fields (only all-blank means zero)', () => {
    const line = ACCOUNT_LAYOUT.format(ACCOUNT_LAYOUT.empty());
    expect(() => ACCOUNT_LAYOUT.parse(' ' + line.slice(1))).toThrow(RecordFormatError);
    expect(ACCOUNT_LAYOUT.parse(' '.repeat(11) + line.slice(11)).acctId).toBe('00000000000');
  });

  it.each([
    ['tcatbal.txt', TRAN_CAT_BAL_LAYOUT],
    ['discgrp.txt', DIS_GROUP_LAYOUT],
  ] as const)('fixed -> JSON -> fixed keeps FILLER bytes (%s)', (file, layout) => {
    const lines = readFileSync(join(DATA, file), 'utf8').split('\n').filter((l) => l.length > 0);
    for (const line of lines) {
      const back = layout.fromJSON(JSON.parse(JSON.stringify(layout.toJSON(layout.parse(line as string)))));
      expect(layout.format(back)).toBe(line);
    }
  });

  it('key functions pad short numeric category codes like the fixed-width format', () => {
    const cat: TranCatRecord = { ...TRAN_CAT_LAYOUT.empty(), tranTypeCd: '01', tranCatCd: '1' };
    expect(tranCatKey(cat)).toBe(tranCatKey(TRAN_CAT_LAYOUT.parse(TRAN_CAT_LAYOUT.format(cat))));
    const dis: DisGroupRecord = { ...DIS_GROUP_LAYOUT.empty(), disAcctGroupId: 'A', disTranTypeCd: '01', disTranCatCd: '1' };
    expect(disGroupKey(dis)).toBe(disGroupKey(DIS_GROUP_LAYOUT.parse(DIS_GROUP_LAYOUT.format(dis))));
  });
});

describe('flat-file DAL edge cases', () => {
  const xref = (card: string, acct: string): CardXrefRecord => ({ xrefCardNum: card, xrefCustId: '000000001', xrefAcctId: acct });

  it('refuses (status 22) a keyed input with duplicate keys and leaves it untouched', async () => {
    const p = join(tmp(), 'dup.txt');
    const body = [xref('1111222233334444', '00000000001'), xref('1111222233334444', '00000000002')]
      .map((r) => CARD_XREF_LAYOUT.format(r) + '\n')
      .join('');
    writeFileSync(p, body);
    const f = new FlatFileKeyedFile(p, CARD_XREF_LAYOUT, cardXrefKey);
    expect(await f.open()).toBe('22');
    expect(readFileSync(p, 'utf8')).toBe(body);
  });

  it('sequential output does not touch the destination until CLOSE', async () => {
    const p = join(tmp(), 'out.txt');
    writeFileSync(p, 'PREVIOUS\n');
    const f = new FlatFileSequentialOutput(p, CARD_XREF_LAYOUT);
    expect(await f.open()).toBe('00');
    await f.write(xref('1111222233334444', '00000000001'));
    expect(readFileSync(p, 'utf8')).toBe('PREVIOUS\n');
    await f.close();
    expect((await readRecords(p, CARD_XREF_LAYOUT))[0]?.xrefAcctId).toBe('00000000001');
  });

  it('writes atomically via a temp file (no partial file, no temp left behind)', async () => {
    const dir = tmp();
    const p = join(dir, 'x.txt');
    await writeRecords(p, CARD_XREF_LAYOUT, [xref('1111222233334444', '00000000001')]);
    expect(readdirSync(dir)).toEqual(['x.txt']);
    await expect(writeRecords(join(dir, 'x.txt', 'nested.txt'), CARD_XREF_LAYOUT, [])).rejects.toThrow();
    expect(existsSync(p)).toBe(true);
  });

  it('with a separate outputPath every OPEN starts again from inputPath', async () => {
    const dir = tmp();
    const f = new FlatFileKeyedFile(join(dir, 'in.txt'), CARD_XREF_LAYOUT, cardXrefKey, {
      outputPath: join(dir, 'out.txt'),
      createIfMissing: true,
    });
    await f.open();
    await f.write(xref('1111222233334444', '00000000001'));
    await f.close();
    await f.open();
    expect((await f.read('1111222233334444')).status).toBe('23');
  });
});

