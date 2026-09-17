import { describe, expect, it } from 'vitest';
import { ACCOUNT_LAYOUT, ACCOUNT_RECORD_LENGTH } from '../src/models/account';
import { CARD_LAYOUT, CARD_RECORD_LENGTH } from '../src/models/card';
import { CARD_XREF_LAYOUT, CARD_XREF_RECORD_LENGTH } from '../src/models/cardXref';
import { CUSTOMER_LAYOUT, CUSTOMER_RECORD_LENGTH } from '../src/models/customer';
import { SEC_USER_LAYOUT, SEC_USER_RECORD_LENGTH } from '../src/models/user';
import { DISCLOSURE_GROUP_LAYOUT, disclosureGroupKey } from '../src/models/disclosureGroup';
import { TRAN_CATEGORY_LAYOUT, tranCategoryKey } from '../src/models/tranCategory';
import { TRAN_TYPE_LAYOUT } from '../src/models/tranType';
import { TRAN_CAT_BAL_LAYOUT, tranCatBalKey } from '../src/models/tranCategoryBalance';
import {
  DAILY_TRANSACTION_LAYOUT,
  TRANSACTION_LAYOUT,
  TRANSACTION_RECORD_LENGTH,
} from '../src/models/transaction';
import { STATEMENT_TRANSACTION_LAYOUT } from '../src/models/statementTransaction';
import { decodeRecord, encodeRecord, layoutLength } from '../src/models/layout';
import { addMoney, multiplyMoney, roundToScale } from '../src/models/decimal';

const layouts = [
  ACCOUNT_LAYOUT,
  CARD_LAYOUT,
  CARD_XREF_LAYOUT,
  CUSTOMER_LAYOUT,
  SEC_USER_LAYOUT,
  DISCLOSURE_GROUP_LAYOUT,
  TRAN_CATEGORY_LAYOUT,
  TRAN_TYPE_LAYOUT,
  TRAN_CAT_BAL_LAYOUT,
  TRANSACTION_LAYOUT,
  DAILY_TRANSACTION_LAYOUT,
  STATEMENT_TRANSACTION_LAYOUT,
];

describe('record layouts', () => {
  it('field lengths add up to the declared LRECL', () => {
    for (const layout of layouts) {
      expect(layoutLength(layout.fields), layout.copybook).toBe(layout.length);
    }
  });

  it('matches the LRECLs documented in README.md', () => {
    expect(ACCOUNT_RECORD_LENGTH).toBe(300);
    expect(CARD_RECORD_LENGTH).toBe(150);
    expect(CUSTOMER_RECORD_LENGTH).toBe(500);
    expect(CARD_XREF_RECORD_LENGTH).toBe(50);
    expect(SEC_USER_RECORD_LENGTH).toBe(80);
    expect(TRANSACTION_RECORD_LENGTH).toBe(350);
  });

  it('round trips an account record through the fixed length image', () => {
    const record = {
      acctId: 11111111111,
      acctActiveStatus: 'Y',
      acctCurrBal: -1234.56,
      acctCreditLimit: 5000,
      acctCashCreditLimit: 500.5,
      acctOpenDate: '2020-01-01',
      acctExpiraionDate: '2025-01-01',
      acctReissueDate: '2023-01-01',
      acctCurrCycCredit: 0,
      acctCurrCycDebit: 99.99,
      acctAddrZip: '12345',
      acctGroupId: 'ZEROAPR',
    };
    const raw = encodeRecord(ACCOUNT_LAYOUT, record);
    expect(raw).toHaveLength(ACCOUNT_RECORD_LENGTH);
    const decoded = decodeRecord(ACCOUNT_LAYOUT, raw);
    expect(decoded.acctCurrBal).toBe(-1234.56);
    expect(decoded.acctCurrCycDebit).toBe(99.99);
    expect(decoded.acctId).toBe(11111111111);
    expect(decoded.acctGroupId.trim()).toBe('ZEROAPR');
  });

  it('keeps signed zoned decimals faithful for both signs and zero', () => {
    for (const balance of [0, 0.01, -0.01, 12345.67, -12345.67]) {
      const record = decodeRecord(
        ACCOUNT_LAYOUT,
        encodeRecord(ACCOUNT_LAYOUT, {
          acctId: 1,
          acctActiveStatus: 'Y',
          acctCurrBal: balance,
          acctCreditLimit: 0,
          acctCashCreditLimit: 0,
          acctOpenDate: '',
          acctExpiraionDate: '',
          acctReissueDate: '',
          acctCurrCycCredit: 0,
          acctCurrCycDebit: 0,
          acctAddrZip: '',
          acctGroupId: '',
        }),
      );
      expect(record.acctCurrBal).toBe(balance);
    }
  });
});

describe('VSAM key formatting', () => {
  it('builds composite keys at the copybook widths', () => {
    expect(
      disclosureGroupKey({ disAcctGroupId: 'ZEROAPR', disTranTypeCd: '01', disTranCatCd: 5 }),
    ).toBe('ZEROAPR   010005');
    expect(tranCategoryKey({ tranTypeCd: '01', tranCatCd: 5 })).toBe('010005');
    expect(tranCatBalKey({ trancatAcctId: 11111111111, trancatTypeCd: '01', trancatCd: 5 })).toBe(
      '11111111111010005',
    );
  });
});

describe('fixed point arithmetic', () => {
  it('avoids binary floating point drift', () => {
    expect(addMoney(0.1, 0.2)).toBe(0.3);
    expect(addMoney(1234.56, -1234.56)).toBe(0);
    expect(multiplyMoney(1000, 0.1275)).toBe(127.5);
    expect(roundToScale(2.005)).toBe(2.01);
  });
});
