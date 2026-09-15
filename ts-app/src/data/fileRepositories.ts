import { readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { decodeLines, type RecordLayout } from '../codec/fixedWidth.js';
import {
  ACCOUNT_LAYOUT,
  CARD_LAYOUT,
  CARD_XREF_LAYOUT,
  CUSTOMER_LAYOUT,
  DAILY_TRANSACTION_LAYOUT,
  SEC_USER_LAYOUT,
  TRAN_CAT_BALANCE_LAYOUT,
  tranCatKey,
  type AccountRecord,
  type CardRecord,
  type CardXrefRecord,
  type CustomerRecord,
  type DailyTransactionRecord,
  type SecUserRecord,
  type TranCatBalanceRecord,
  type TransactionRecord,
} from '../domain/index.js';
import { InMemoryKeyedRepository, type CardDemoRepositories } from './repositories.js';

const here = dirname(fileURLToPath(import.meta.url));

/** Repository root, i.e. the directory holding both `app/` and `ts-app/`. */
export const repoRoot = resolve(here, '../../..');
/** Fixed-width sample data shipped with the COBOL application. */
export const asciiDataDir = join(repoRoot, 'app', 'data', 'ASCII');
/** Fixtures owned by the TypeScript app (data the COBOL repo only ships as EBCDIC or in JCL). */
export const tsDataDir = join(repoRoot, 'ts-app', 'data');

export function loadRecords<T>(layout: RecordLayout<T>, path: string): T[] {
  return decodeLines(layout, readFileSync(path, 'utf8'));
}

export interface FileRepositoryOptions {
  /** Defaults to `app/data/ASCII`. */
  dataDir?: string;
  /** Defaults to `ts-app/data/usrsec.txt` (see that file's header for provenance). */
  usrsecPath?: string;
  /** Defaults to `app/data/ASCII/dailytran.txt`. */
  dailyTranPath?: string;
}

export function createFileBackedRepositories(
  options: FileRepositoryOptions = {},
): CardDemoRepositories {
  const dataDir = options.dataDir ?? asciiDataDir;
  const usrsecPath = options.usrsecPath ?? join(tsDataDir, 'usrsec.txt');
  const dailyTranPath = options.dailyTranPath ?? join(dataDir, 'dailytran.txt');

  const users = loadRecords<SecUserRecord>(SEC_USER_LAYOUT, usrsecPath);
  const accounts = loadRecords<AccountRecord>(ACCOUNT_LAYOUT, join(dataDir, 'acctdata.txt'));
  const cards = loadRecords<CardRecord>(CARD_LAYOUT, join(dataDir, 'carddata.txt'));
  const xrefs = loadRecords<CardXrefRecord>(CARD_XREF_LAYOUT, join(dataDir, 'cardxref.txt'));
  const customers = loadRecords<CustomerRecord>(CUSTOMER_LAYOUT, join(dataDir, 'custdata.txt'));
  const balances = loadRecords<TranCatBalanceRecord>(
    TRAN_CAT_BALANCE_LAYOUT,
    join(dataDir, 'tcatbal.txt'),
  );
  const dailyTransactions = loadRecords<DailyTransactionRecord>(
    DAILY_TRANSACTION_LAYOUT,
    dailyTranPath,
  );

  return {
    users: new InMemoryKeyedRepository<SecUserRecord>((record) => record.secUsrId, users),
    accounts: new InMemoryKeyedRepository<AccountRecord>((record) => record.acctId, accounts),
    cards: new InMemoryKeyedRepository<CardRecord>((record) => record.cardNum, cards),
    cardXrefs: new InMemoryKeyedRepository<CardXrefRecord>((record) => record.xrefCardNum, xrefs),
    cardXrefsByAccount: new InMemoryKeyedRepository<CardXrefRecord>(
      (record) => record.xrefAcctId,
      xrefs,
    ),
    customers: new InMemoryKeyedRepository<CustomerRecord>((record) => record.custId, customers),
    tranCatBalances: new InMemoryKeyedRepository<TranCatBalanceRecord>(
      (record) => tranCatKey(record.trancatAcctId, record.trancatTypeCd, record.trancatCd),
      balances,
    ),
    // CBTRN02C opens TRANSACT with OPEN OUTPUT, so the posting run starts empty.
    transactions: new InMemoryKeyedRepository<TransactionRecord>((record) => record.tranId),
    dailyTransactions,
  };
}
