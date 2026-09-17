import { beforeEach, describe, expect, it } from 'vitest';

import { CardDemoStore } from '../src/data/store.js';
import { InMemoryStorage } from '../src/data/storage.js';
import { DatasetError, FileStatus } from '../src/data/status.js';
import { seedAll } from '../src/loaders/seed.js';

function seededStore(encoding: 'ascii' | 'ebcdic' = 'ascii'): CardDemoStore {
  const store = new CardDemoStore(new InMemoryStorage());
  seedAll(store, { encoding });
  return store;
}

describe('seed loaders', () => {
  it('loads every dataset from the ASCII sample data', () => {
    const store = new CardDemoStore(new InMemoryStorage());
    const results = seedAll(store, { encoding: 'ascii' });
    const counts = Object.fromEntries(results.map((result) => [result.dataset, result.records]));

    expect(counts).toMatchObject({
      ACCTDAT: 50,
      CARDDAT: 50,
      CUSTDAT: 50,
      CCXREF: 50,
      DISCGRP: 51,
      TCATBALF: 50,
      TRANCATG: 18,
      TRANTYPE: 7,
      USRSEC: 10,
      DALYTRAN: 300,
    });
  });

  it('decodes the EBCDIC sample data to the same balances as the ASCII copy', () => {
    // The two copies differ only in ACCT-GROUP-ID of account 49 (ZEROAPR vs A000000000).
    const ascii = seededStore('ascii').open('ACCTDAT').readAll();
    const ebcdic = seededStore('ebcdic').open('ACCTDAT').readAll();

    expect(ebcdic.map((account) => account.acctId)).toEqual(ascii.map((account) => account.acctId));
    expect(ebcdic.map((account) => account.acctCurrBal)).toEqual(ascii.map((account) => account.acctCurrBal));
    expect(ebcdic.map((account) => account.acctCreditLimit)).toEqual(
      ascii.map((account) => account.acctCreditLimit),
    );
  });
});

describe('KSDS access', () => {
  let store: CardDemoStore;

  beforeEach(() => {
    store = seededStore();
  });

  it('reads by primary key', () => {
    const account = store.open('ACCTDAT').read(11);
    expect(account.acctId).toBe(11);
    expect(account.acctActiveStatus).toBe('Y');
  });

  it('raises file status 23 for a missing key', () => {
    expect(() => store.open('ACCTDAT').read(99999999999)).toThrowError(DatasetError);
    try {
      store.open('ACCTDAT').read(99999999999);
    } catch (error) {
      expect((error as DatasetError).status).toBe(FileStatus.NotFound);
    }
  });

  it('browses in key order from a starting key', () => {
    const browse = store.open('CUSTDAT').startBrowse(5);
    expect(browse.readNext()?.custId).toBe(5);
    expect(browse.readNext()?.custId).toBe(6);
  });

  it('reads cards by the CARDAIX alternate index on account id', () => {
    const cards = store.open('CARDDAT').readByAlternateIndex('CARDAIX', 11);
    expect(cards.length).toBeGreaterThan(0);
    for (const card of cards) expect(card.cardAcctId).toBe(11);
  });

  it('reads the xref by the CXACAIX alternate index on account id', () => {
    const xrefs = store.open('CCXREF').readByAlternateIndex('CXACAIX', 11);
    expect(xrefs.length).toBeGreaterThan(0);
    for (const xref of xrefs) expect(xref.xrefAcctId).toBe(11);
  });

  it('writes, rewrites and deletes records', () => {
    const accounts = store.open('ACCTDAT');
    const account = accounts.read(1);

    accounts.rewrite({ ...account, acctCurrBal: 250.75 });
    expect(accounts.read(1).acctCurrBal).toBe(250.75);

    expect(() => accounts.write(account)).toThrowError(/duplicate key/);

    accounts.delete(1);
    expect(accounts.tryRead(1)).toBeUndefined();

    accounts.write(account);
    expect(accounts.read(1).acctCurrBal).toBe(account.acctCurrBal);
  });

  it('authenticates users against the security file', () => {
    const user = store.open('USRSEC').read('ADMIN001');
    expect(user.secUsrPwd.trim()).toBe('PASSWORD');
    expect(user.secUsrType).toBe('A');
  });
});

describe('sequential access', () => {
  it('reads daily transactions in arrival order', () => {
    const daily = seededStore().openSequential('DALYTRAN');
    const first = daily.readNext();
    const second = daily.readNext();

    expect(first?.dalytranId).toHaveLength(16);
    expect(first?.dalytranAmt).toBeTypeOf('number');
    expect(second?.dalytranId).not.toBe(first?.dalytranId);
    expect(daily.readAll()).toHaveLength(300);
  });
});
