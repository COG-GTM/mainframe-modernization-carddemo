import { dec } from '../src/copybooks/numeric.js';
import {
  type AccountRecord,
  accountKey,
  type CardXrefRecord,
  cardXrefKey,
  type DailyTranRecord,
  type TranCatBalRecord,
  tranCatBalKey,
  type TranRecord,
  tranKey,
} from '../src/copybooks/index.js';
import type { PostTranFiles, RejectRecord } from '../src/cbtrn02c/index.js';
import { InMemoryKeyedFile, InMemorySequentialInput, InMemorySequentialOutput } from '../src/dal/index.js';

export const CARD = '4859452612877065';
export const ACCT = '00000000001';
export const FIXED_CLOCK = (): Date => new Date(2022, 5, 11, 0, 0, 0, 340);

export function dailyTran(overrides: Partial<DailyTranRecord> = {}): DailyTranRecord {
  return {
    dalytranId: '0000000000000001',
    dalytranTypeCd: '01',
    dalytranCatCd: '0001',
    dalytranSource: 'POS TERM',
    dalytranDesc: 'Purchase at Test Merchant',
    dalytranAmt: dec('100.00'),
    dalytranMerchantId: '800000000',
    dalytranMerchantName: 'Test Merchant',
    dalytranMerchantCity: 'Springfield',
    dalytranMerchantZip: '12345',
    dalytranCardNum: CARD,
    dalytranOrigTs: '2022-06-10 19:27:53.000000',
    dalytranProcTs: '',
    ...overrides,
  };
}

export function account(overrides: Partial<AccountRecord> = {}): AccountRecord {
  return {
    acctId: ACCT,
    acctActiveStatus: 'Y',
    acctCurrBal: dec('500.00'),
    acctCreditLimit: dec('1000.00'),
    acctCashCreditLimit: dec('500.00'),
    acctOpenDate: '2014-11-20',
    acctExpirationDate: '2025-05-20',
    acctReissueDate: '2025-05-20',
    acctCurrCycCredit: dec('0'),
    acctCurrCycDebit: dec('0'),
    acctAddrZip: '',
    acctGroupId: 'A000000000',
    ...overrides,
  };
}

export const xref = (card = CARD, acct = ACCT): CardXrefRecord => ({
  xrefCardNum: card,
  xrefCustId: '000000001',
  xrefAcctId: acct,
});

export interface Harness {
  files: PostTranFiles;
  rejects: InMemorySequentialOutput<RejectRecord>;
  transactions: InMemoryKeyedFile<TranRecord>;
  accounts: InMemoryKeyedFile<AccountRecord>;
  tcatbal: InMemoryKeyedFile<TranCatBalRecord>;
}

export function harness(opts: {
  trans: DailyTranRecord[];
  xrefs?: CardXrefRecord[];
  accounts?: AccountRecord[];
  tcatbal?: TranCatBalRecord[];
  existingTransactions?: TranRecord[];
}): Harness {
  const rejects = new InMemorySequentialOutput<RejectRecord>();
  const transactions = new InMemoryKeyedFile(tranKey, opts.existingTransactions ?? []);
  const accounts = new InMemoryKeyedFile(accountKey, opts.accounts ?? [account()]);
  const tcatbal = new InMemoryKeyedFile(tranCatBalKey, opts.tcatbal ?? []);
  return {
    files: {
      dalytran: new InMemorySequentialInput(opts.trans),
      tranfile: transactions,
      xreffile: new InMemoryKeyedFile(cardXrefKey, opts.xrefs ?? [xref()]),
      dalyrejs: rejects,
      acctfile: accounts,
      tcatbalf: tcatbal,
    },
    rejects,
    transactions,
    accounts,
    tcatbal,
  };
}
