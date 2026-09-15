import { Decimal } from 'decimal.js';
import { describe, expect, it } from 'vitest';
import {
  decodeRecord,
  encodeRecord,
  layoutLength,
  type RecordLayout,
} from '../../src/codec/fixedWidth.js';
import { decodeZoned, encodeZoned } from '../../src/codec/zonedDecimal.js';
import {
  ACCOUNT_LAYOUT,
  CARD_XREF_LAYOUT,
  CUSTOMER_LAYOUT,
  DAILY_TRANSACTION_LAYOUT,
  SEC_USER_LAYOUT,
  type AccountRecord,
  type DailyTransactionRecord,
} from '../../src/domain/index.js';
import { ACCOUNT } from './fixtures.js';

/** Parity reference: the copybooks in `app/cpy/` and the files in `app/data/ASCII/`. */
describe('zoned decimal overpunch', () => {
  it.each([
    ['00000001940{', '194.00'],
    ['00000001940A', '194.01'],
    ['00000000000I', '0.09'],
    ['0000000194A', '19.41'],
  ])('decodes the positive value %s', (raw, expected) => {
    expect(decodeZoned(raw, 2).toFixed(2)).toBe(expected);
  });

  it.each([
    ['00000001940}', '-194.00'],
    ['00000001940J', '-194.01'],
    ['00000000000R', '-0.09'],
  ])('decodes the negative value %s', (raw, expected) => {
    expect(decodeZoned(raw, 2).toFixed(2)).toBe(expected);
  });

  it('round-trips through encodeZoned', () => {
    for (const text of ['194.00', '-194.01', '0.00', '-0.09', '99999999.99']) {
      const value = new Decimal(text);
      const encoded = encodeZoned(value, 12, 2);
      expect(encoded).toHaveLength(12);
      expect(decodeZoned(encoded, 2).toFixed(2)).toBe(value.toFixed(2));
    }
  });

  it('keeps the sign on the low-order digit', () => {
    expect(encodeZoned(new Decimal('194.00'), 12, 2)).toBe('00000001940{');
    expect(encodeZoned(new Decimal('-194.01'), 12, 2)).toBe('00000001940J');
  });
});

describe('fixed-width record codec', () => {
  const layouts: readonly RecordLayout<unknown>[] = [
    ACCOUNT_LAYOUT as RecordLayout<unknown>,
    CARD_XREF_LAYOUT as RecordLayout<unknown>,
    CUSTOMER_LAYOUT as RecordLayout<unknown>,
    DAILY_TRANSACTION_LAYOUT as RecordLayout<unknown>,
    SEC_USER_LAYOUT as RecordLayout<unknown>,
  ];

  it.each(layouts.map((layout) => [layout.copybook, layout] as const))(
    '%s field widths add up to the copybook RECLN',
    (_copybook, layout) => {
      expect(layoutLength(layout.fields)).toBe(layout.recordLength);
    },
  );

  it('round-trips an ACCOUNT-RECORD (CVACT01Y)', () => {
    const encoded = encodeRecord(ACCOUNT_LAYOUT, ACCOUNT);

    expect(encoded).toHaveLength(ACCOUNT_LAYOUT.recordLength);
    expect(encoded.slice(0, 12)).toBe('00000000011Y');
    expect(decodeRecord(ACCOUNT_LAYOUT, encoded)).toEqual(ACCOUNT);
  });

  it('decodes an ACCOUNT-RECORD line from app/data/ASCII/acctdata.txt', () => {
    const line =
      '00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20' +
      '00000000000{00000000000{A000000000';
    const record: AccountRecord = decodeRecord(ACCOUNT_LAYOUT, line);

    expect(record.acctId).toBe('00000000001');
    expect(record.acctCurrBal.toFixed(2)).toBe('194.00');
    expect(record.acctCreditLimit.toFixed(2)).toBe('2020.00');
    expect(record.acctCashCreditLimit.toFixed(2)).toBe('1020.00');
    expect(record.acctOpenDate).toBe('2014-11-20');
    expect(record.acctGroupId).toBe('');
    expect(encodeRecord(ACCOUNT_LAYOUT, record).trimEnd()).toBe(line.trimEnd());
  });

  it('round-trips a DALYTRAN-RECORD (CVTRA06Y)', () => {
    const record: DailyTransactionRecord = {
      dalytranId: '0000000000068358',
      dalytranTypeCd: '01',
      dalytranCatCd: '0001',
      dalytranSource: 'POS TERM',
      dalytranDesc: 'Purchase at Abshire-Lowe',
      dalytranAmt: new Decimal('-38.25'),
      dalytranMerchantId: '123456789',
      dalytranMerchantName: 'Abshire-Lowe',
      dalytranMerchantCity: 'Springfield',
      dalytranMerchantZip: '12345',
      dalytranCardNum: '4111111111111111',
      dalytranOrigTs: '2022-06-24 10:11:12.000000',
      dalytranProcTs: '2022-06-24 10:11:13.000000',
    };

    const encoded = encodeRecord(DAILY_TRANSACTION_LAYOUT, record);

    expect(encoded).toHaveLength(DAILY_TRANSACTION_LAYOUT.recordLength);
    expect(decodeRecord(DAILY_TRANSACTION_LAYOUT, encoded)).toEqual(record);
    expect(decodeRecord(DAILY_TRANSACTION_LAYOUT, encoded).dalytranAmt).toBeInstanceOf(Decimal);
  });

  it('pads short lines, as several sample files drop trailing FILLER', () => {
    const xref = decodeRecord(CARD_XREF_LAYOUT, '050002445376574000000005000000000050');

    expect(xref).toEqual({
      xrefCardNum: '0500024453765740',
      xrefCustId: '000000050',
      xrefAcctId: '00000000050',
    });
    expect(encodeRecord(CARD_XREF_LAYOUT, xref)).toHaveLength(CARD_XREF_LAYOUT.recordLength);
  });
});
