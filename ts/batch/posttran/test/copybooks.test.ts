import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { describe, expect, it } from 'vitest';
import {
  ACCOUNT_LAYOUT,
  CARD_LAYOUT,
  CARD_XREF_LAYOUT,
  DAILY_TRAN_LAYOUT,
  DIS_GROUP_LAYOUT,
  type RecordLayout,
  TRAN_CAT_BAL_LAYOUT,
  TRAN_CAT_LAYOUT,
  TRAN_LAYOUT,
  TRAN_TYPE_LAYOUT,
} from '../src/copybooks/index.js';

const DATA = fileURLToPath(new URL('../../../../app/data/ASCII/', import.meta.url));

const lines = (file: string): string[] =>
  readFileSync(DATA + file, 'utf8').split('\n').filter((l) => l.length > 0);

describe('copybook record lengths match the COBOL FDs / LRECLs', () => {
  it.each<[RecordLayout<unknown>, number]>([
    [DAILY_TRAN_LAYOUT as RecordLayout<unknown>, 350],
    [TRAN_LAYOUT as RecordLayout<unknown>, 350],
    [ACCOUNT_LAYOUT as RecordLayout<unknown>, 300],
    [CARD_LAYOUT as RecordLayout<unknown>, 150],
    [CARD_XREF_LAYOUT as RecordLayout<unknown>, 50],
    [TRAN_CAT_BAL_LAYOUT as RecordLayout<unknown>, 50],
    [DIS_GROUP_LAYOUT as RecordLayout<unknown>, 50],
    [TRAN_TYPE_LAYOUT as RecordLayout<unknown>, 60],
    [TRAN_CAT_LAYOUT as RecordLayout<unknown>, 60],
  ])('%s', (layout, length) => {
    expect(layout.length).toBe(length);
  });
});

describe('ASCII sample data round-trips byte-for-byte', () => {
  it.each<[string, RecordLayout<unknown>]>([
    ['dailytran.txt', DAILY_TRAN_LAYOUT as RecordLayout<unknown>],
    ['acctdata.txt', ACCOUNT_LAYOUT as RecordLayout<unknown>],
    ['carddata.txt', CARD_LAYOUT as RecordLayout<unknown>],
    ['cardxref.txt', CARD_XREF_LAYOUT as RecordLayout<unknown>],
    ['tcatbal.txt', TRAN_CAT_BAL_LAYOUT as RecordLayout<unknown>],
    ['discgrp.txt', DIS_GROUP_LAYOUT as RecordLayout<unknown>],
    ['trantype.txt', TRAN_TYPE_LAYOUT as RecordLayout<unknown>],
    ['trancatg.txt', TRAN_CAT_LAYOUT as RecordLayout<unknown>],
  ])('%s', (file, layout) => {
    for (const line of lines(file)) {
      expect(layout.format(layout.parse(line))).toBe(line.padEnd(layout.length, ' '));
    }
  });

  it('INITIALIZE resets named fields but keeps FILLER bytes', () => {
    const rec = TRAN_CAT_BAL_LAYOUT.parse(lines('tcatbal.txt')[0] as string);
    const init = TRAN_CAT_BAL_LAYOUT.initialize(rec);
    expect(init.trancatAcctId).toBe('00000000000');
    expect(init.tranCatBal.isZero()).toBe(true);
    expect(TRAN_CAT_BAL_LAYOUT.format(init).slice(28)).toBe('0'.repeat(22));
    expect(TRAN_CAT_BAL_LAYOUT.format(TRAN_CAT_BAL_LAYOUT.empty()).slice(28)).toBe(' '.repeat(22));
  });

  it('JSON round-trip preserves values', () => {
    for (const line of lines('acctdata.txt')) {
      const rec = ACCOUNT_LAYOUT.parse(line);
      const back = ACCOUNT_LAYOUT.fromJSON(JSON.parse(JSON.stringify(ACCOUNT_LAYOUT.toJSON(rec))));
      expect(ACCOUNT_LAYOUT.format(back)).toBe(ACCOUNT_LAYOUT.format(rec));
    }
  });
});

describe('field decoding', () => {
  it('parses the first daily transaction', () => {
    const rec = DAILY_TRAN_LAYOUT.parse(lines('dailytran.txt')[0] as string);
    expect(rec.dalytranId).toBe('0000000000683580');
    expect(rec.dalytranTypeCd).toBe('01');
    expect(rec.dalytranCatCd).toBe('0001');
    expect(rec.dalytranSource).toBe('POS TERM');
    expect(rec.dalytranAmt.toFixed(2)).toBe('504.77');
    expect(rec.dalytranMerchantName).toBe('Abshire-Lowe');
    expect(rec.dalytranCardNum).toBe('4859452612877065');
    expect(rec.dalytranOrigTs).toBe('2022-06-10 19:27:53.000000');
    expect(rec.dalytranProcTs).toBe('');
  });

  it('parses the first account', () => {
    const rec = ACCOUNT_LAYOUT.parse(lines('acctdata.txt')[0] as string);
    expect(rec.acctId).toBe('00000000001');
    expect(rec.acctActiveStatus).toBe('Y');
    expect(rec.acctCurrBal.toFixed(2)).toBe('194.00');
    expect(rec.acctCreditLimit.toFixed(2)).toBe('2020.00');
    expect(rec.acctExpirationDate).toBe('2025-05-20');
    // The sample data carries the disclosure group id in the ACCT-ADDR-ZIP
    // position of CVACT01Y; kept as-is for byte-level parity.
    expect(rec.acctAddrZip).toBe('A000000000');
    expect(rec.acctGroupId).toBe('');
  });
});
