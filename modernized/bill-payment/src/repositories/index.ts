import { Account } from '../models/account';
import { CardXref } from '../models/cardXref';
import { Transaction } from '../models/transaction';

/**
 * Repository interfaces — the modern stand-ins for CICS file control over the
 * VSAM datasets used by COBIL00C:
 *   ACCTDAT  (KSDS)              -> AccountRepository
 *   CXACAIX  (alternate index)  -> CardXrefRepository
 *   TRANSACT (KSDS)             -> TransactionRepository
 */

export interface AccountRepository {
  /** EXEC CICS READ ... DATASET(ACCTDAT) — returns null on NOTFND. */
  findById(acctId: string): Promise<Account | null>;
  /** EXEC CICS REWRITE ... DATASET(ACCTDAT). */
  update(account: Account): Promise<void>;
}

export interface CardXrefRepository {
  /** EXEC CICS READ ... DATASET(CXACAIX) RIDFLD(acctId) — returns null on NOTFND. */
  findByAcctId(acctId: string): Promise<CardXref | null>;
}

export interface TransactionRepository {
  /**
   * Equivalent to STARTBR(HIGH-VALUES) + READPREV + ENDBR: returns the highest
   * numeric transaction id currently on file, or 0 when the file is empty.
   */
  maxTransactionId(): Promise<number>;
  /** EXEC CICS WRITE ... DATASET(TRANSACT) — throws on duplicate key. */
  insert(transaction: Transaction): Promise<void>;
  /** Test/inspection helper. */
  findById(transactionId: string): Promise<Transaction | null>;
}

export interface Repositories {
  accounts: AccountRepository;
  cardXrefs: CardXrefRepository;
  transactions: TransactionRepository;
}

export * from './inMemory';
