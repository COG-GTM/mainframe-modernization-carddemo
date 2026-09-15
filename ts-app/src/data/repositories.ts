import type {
  AccountRecord,
  CardRecord,
  CardXrefRecord,
  CustomerRecord,
  DailyTransactionRecord,
  SecUserRecord,
  TranCatBalanceRecord,
  TransactionRecord,
} from '../domain/index.js';

/**
 * A VSAM KSDS file: random reads by record key plus rewrite/write.
 * `read` returns `undefined` where COBOL would take the INVALID KEY /
 * DFHRESP(NOTFND) path.
 */
export interface KeyedRepository<T> {
  read(key: string): T | undefined;
  write(key: string, record: T): void;
  values(): T[];
}

/** A sequential (QSAM) file opened for output, e.g. the rejects file. */
export interface SequentialWriter<T> {
  write(record: T): void;
  records(): T[];
}

export class InMemoryKeyedRepository<T> implements KeyedRepository<T> {
  private readonly store = new Map<string, T>();

  constructor(
    private readonly keyOf: (record: T) => string,
    records: readonly T[] = [],
  ) {
    for (const record of records) this.store.set(keyOf(record), record);
  }

  keyFor(record: T): string {
    return this.keyOf(record);
  }

  read(key: string): T | undefined {
    return this.store.get(key);
  }

  write(key: string, record: T): void {
    this.store.set(key, record);
  }

  values(): T[] {
    return [...this.store.values()];
  }
}

export class InMemorySequentialWriter<T> implements SequentialWriter<T> {
  private readonly written: T[] = [];

  write(record: T): void {
    this.written.push(record);
  }

  records(): T[] {
    return [...this.written];
  }
}

/** The set of files the migrated programs need, mirroring the CICS/JCL DD names. */
export interface CardDemoRepositories {
  /** USRSEC — keyed by SEC-USR-ID */
  users: KeyedRepository<SecUserRecord>;
  /** ACCTDAT — keyed by ACCT-ID */
  accounts: KeyedRepository<AccountRecord>;
  /** CARDDAT — keyed by CARD-NUM */
  cards: KeyedRepository<CardRecord>;
  /** CARDXREF — keyed by XREF-CARD-NUM */
  cardXrefs: KeyedRepository<CardXrefRecord>;
  /** CXACAIX — the alternate index path over CARDXREF, keyed by XREF-ACCT-ID */
  cardXrefsByAccount: KeyedRepository<CardXrefRecord>;
  /** CUSTDAT — keyed by CUST-ID */
  customers: KeyedRepository<CustomerRecord>;
  /** TCATBALF — keyed by TRAN-CAT-KEY */
  tranCatBalances: KeyedRepository<TranCatBalanceRecord>;
  /** TRANSACT — keyed by TRAN-ID */
  transactions: KeyedRepository<TransactionRecord>;
  /** DALYTRAN — read sequentially by the posting job */
  dailyTransactions: readonly DailyTransactionRecord[];
}
