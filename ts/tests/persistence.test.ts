import { beforeAll, describe, expect, it } from 'vitest';
import { DataStore } from '../src/data/dataStore';
import { DuplicateRecordError, RecordNotFoundError } from '../src/data/errors';
import { splitFixedWidth } from '../src/data/fixedWidthFile';
import { AccountRecord } from '../src/models/account';

const store = new DataStore();

beforeAll(() => {
  store.loadSampleData();
});

describe('sample data loading', () => {
  it('loads every seeded dataset', () => {
    expect(store.users.readAll()).toHaveLength(10);
    expect(store.accounts.readAll().length).toBeGreaterThan(0);
    expect(store.cards.readAll().length).toBeGreaterThan(0);
    expect(store.customers.readAll().length).toBeGreaterThan(0);
    expect(store.cardXref.readAll().length).toBeGreaterThan(0);
    expect(store.dailyTransactions.readAll().length).toBeGreaterThan(0);
    expect(store.disclosureGroups.readAll().length).toBeGreaterThan(0);
    expect(store.tranCategories.readAll().length).toBeGreaterThan(0);
    expect(store.tranTypes.readAll().length).toBeGreaterThan(0);
    expect(store.tranCatBalances.readAll().length).toBeGreaterThan(0);
  });

  it('decodes account balances as signed amounts', () => {
    for (const account of store.accounts.readAll()) {
      expect(Number.isFinite(account.acctCurrBal)).toBe(true);
      expect(account.acctId).toBeGreaterThan(0);
    }
  });

  it('cross references resolve card -> account -> customer', () => {
    const xref = store.cardXref.readAll()[0] as { xrefAcctId: number; xrefCustId: number };
    expect(store.accounts.tryRead(xref.xrefAcctId)).toBeDefined();
    expect(store.customers.tryRead(xref.xrefCustId)).toBeDefined();
  });

  it('splits records with and without line separators', () => {
    expect(splitFixedWidth('abcde', 5)).toEqual(['abcde']);
    expect(splitFixedWidth('abcdefghij', 5)).toEqual(['abcde', 'fghij']);
    expect(splitFixedWidth('abcde\nfghij\n', 5)).toEqual(['abcde', 'fghij']);
  });
});

describe('KSDS behaviour', () => {
  it('reads by alternate index', () => {
    const card = store.cards.readAll()[0];
    expect(card).toBeDefined();
    const byAccount = store.cards.readByAccountId((card as { cardAcctId: number }).cardAcctId);
    expect(byAccount.map((c) => c.cardNum)).toContain(card?.cardNum);
  });

  it('browses forward and backward in key order', () => {
    const all = store.accounts.readAll();
    const browse = store.accounts.startBrowse();
    expect(browse.readNext().acctId).toBe(all[0]?.acctId);
    expect(browse.readNext().acctId).toBe(all[1]?.acctId);
    expect(browse.readPrev().acctId).toBe(all[1]?.acctId);
    expect(browse.readPrev().acctId).toBe(all[0]?.acctId);
    expect(browse.tryReadPrev()).toBeUndefined();
  });

  it('enforces NOTFND and DUPREC conditions', () => {
    const scratch = new DataStore();
    const account: AccountRecord = {
      acctId: 99999999999,
      acctActiveStatus: 'Y',
      acctCurrBal: 10,
      acctCreditLimit: 100,
      acctCashCreditLimit: 50,
      acctOpenDate: '2024-01-01',
      acctExpiraionDate: '2029-01-01',
      acctReissueDate: '2024-01-01',
      acctCurrCycCredit: 0,
      acctCurrCycDebit: 0,
      acctAddrZip: '12345',
      acctGroupId: 'ZEROAPR',
    };
    expect(() => scratch.accounts.read(account.acctId)).toThrow(RecordNotFoundError);
    scratch.accounts.write(account);
    expect(scratch.accounts.read(account.acctId).acctCurrBal).toBe(10);
    expect(() => scratch.accounts.write(account)).toThrow(DuplicateRecordError);
    scratch.accounts.rewrite({ ...account, acctCurrBal: 25.5 });
    expect(scratch.accounts.read(account.acctId).acctCurrBal).toBe(25.5);
    scratch.accounts.delete(account.acctId);
    expect(scratch.accounts.tryRead(account.acctId)).toBeUndefined();
    expect(() => scratch.accounts.delete(account.acctId)).toThrow(RecordNotFoundError);
  });
});
