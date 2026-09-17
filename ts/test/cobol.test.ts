import { describe, expect, it } from 'vitest';

import { addDecimal, divideDecimal, multiplyDecimal, roundHalfUp, subtractDecimal, truncate } from '../src/cobol/decimal.js';
import { decodeEbcdic, encodeEbcdic } from '../src/cobol/ebcdic.js';
import { parsePicture } from '../src/cobol/picture.js';
import { decodeRecord, encodeRecord, splitTextRecords } from '../src/cobol/record.js';
import { ACCOUNT_RECORD_SPEC } from '../src/models/account.js';
import { TRANSACTION_RECORD_SPEC } from '../src/models/transaction.js';

describe('parsePicture', () => {
  it('parses signed packed and display numerics', () => {
    expect(parsePicture('S9(10)V99')).toMatchObject({ digits: 12, scale: 2, signed: true, length: 12 });
    expect(parsePicture('9(11)')).toMatchObject({ digits: 11, scale: 0, signed: false, length: 11 });
    expect(parsePicture('X(50)')).toMatchObject({ kind: 'alphanumeric', length: 50 });
    expect(parsePicture('S9(04)V99', 'COMP-3')).toMatchObject({ packed: true, digits: 6, length: 4 });
  });
});

describe('decimal arithmetic', () => {
  it('adds and subtracts without binary floating point drift', () => {
    expect(addDecimal(0.1, 0.2)).toBe(0.3);
    expect(subtractDecimal(1000000000.05, 0.06)).toBe(999999999.99);
  });

  it('rounds half-up away from zero, as COBOL ROUNDED does', () => {
    expect(roundHalfUp(2.345, 2)).toBe(2.35);
    expect(roundHalfUp(-2.345, 2)).toBe(-2.35);
    expect(truncate(2.349, 2)).toBe(2.34);
    expect(truncate(-2.349, 2)).toBe(-2.34);
  });

  it('matches the CBACT04C monthly interest formula', () => {
    // COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
    const balance = 1234.56;
    const rate = 17.25;
    expect(divideDecimal(multiplyDecimal(balance, rate, 2, 2), 1200, 2, 2)).toBe(17.75);
  });
});

describe('EBCDIC codec', () => {
  it('round-trips CP037 bytes', () => {
    const ascii = Buffer.from('CARDDEMO 0123', 'latin1');
    expect(decodeEbcdic(encodeEbcdic(ascii)).toString('latin1')).toBe('CARDDEMO 0123');
  });
});

describe('fixed-length record codec', () => {
  const rawAccount =
    '00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000'.padEnd(
      300,
      ' ',
    );

  it('decodes zoned decimal overpunch signs', () => {
    const account = decodeRecord(ACCOUNT_RECORD_SPEC, rawAccount);
    expect(account.acctId).toBe(1);
    expect(account.acctActiveStatus).toBe('Y');
    expect(account.acctCurrBal).toBe(194);
    expect(account.acctCreditLimit).toBe(2020);
    expect(account.acctCashCreditLimit).toBe(1020);
    expect(account.acctOpenDate).toBe('2014-11-20');
    expect(account.acctAddrZip).toBe('A000000000');
  });

  it('re-encodes to the original bytes', () => {
    const account = decodeRecord(ACCOUNT_RECORD_SPEC, rawAccount);
    expect(encodeRecord(ACCOUNT_RECORD_SPEC, account)).toBe(rawAccount);
  });

  it('encodes negative amounts with the negative overpunch', () => {
    const account = decodeRecord(ACCOUNT_RECORD_SPEC, rawAccount);
    const encoded = encodeRecord(ACCOUNT_RECORD_SPEC, { ...account, acctCurrBal: -194.05 });
    expect(encoded.slice(12, 24)).toBe('00000001940N');
    expect(decodeRecord(ACCOUNT_RECORD_SPEC, encoded).acctCurrBal).toBe(-194.05);
  });

  it('keeps the copybook record lengths', () => {
    expect(ACCOUNT_RECORD_SPEC.recordLength).toBe(300);
    expect(TRANSACTION_RECORD_SPEC.recordLength).toBe(350);
  });

  it('pads text records whose trailing spaces were stripped', () => {
    const records = splitTextRecords(Buffer.from('ABC\nDEF\n', 'latin1'), 10);
    expect(records).toHaveLength(2);
    expect(records[0]?.toString('latin1')).toBe('ABC       ');
  });
});
