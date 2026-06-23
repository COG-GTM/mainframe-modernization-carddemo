import { Account } from '../models/account';
import { CardXref } from '../models/cardXref';
import { Transaction, TRAN_ID_LENGTH } from '../models/transaction';
import { Money } from '../money';
import { BillPaymentError } from '../errors';
import {
  AccountRepository,
  CardXrefRepository,
  Repositories,
  TransactionRepository,
} from './index';

/**
 * In-memory implementations of the VSAM-backed repositories. These stand in for
 * ACCTDAT / CXACAIX / TRANSACT so the modernized service is runnable and
 * testable without a mainframe. Swap these for a real database (e.g. Postgres)
 * in production — see MODERNIZATION_NOTES.md.
 */

export class InMemoryAccountRepository implements AccountRepository {
  private readonly accounts = new Map<string, Account>();

  constructor(seed: Account[] = []) {
    for (const a of seed) this.accounts.set(a.acctId, { ...a });
  }

  async findById(acctId: string): Promise<Account | null> {
    const found = this.accounts.get(acctId);
    return found ? { ...found } : null;
  }

  async update(account: Account): Promise<void> {
    if (!this.accounts.has(account.acctId)) {
      throw new BillPaymentError('UPDATE_FAILED');
    }
    this.accounts.set(account.acctId, { ...account });
  }
}

export class InMemoryCardXrefRepository implements CardXrefRepository {
  private readonly byAcct = new Map<string, CardXref>();

  constructor(seed: CardXref[] = []) {
    for (const x of seed) this.byAcct.set(x.acctId, { ...x });
  }

  async findByAcctId(acctId: string): Promise<CardXref | null> {
    const found = this.byAcct.get(acctId);
    return found ? { ...found } : null;
  }
}

export class InMemoryTransactionRepository implements TransactionRepository {
  private readonly byId = new Map<string, Transaction>();

  constructor(seed: Transaction[] = []) {
    for (const t of seed) this.byId.set(t.transactionId, { ...t });
  }

  async maxTransactionId(): Promise<number> {
    let max = 0;
    for (const id of this.byId.keys()) {
      const n = Number(id);
      if (Number.isFinite(n) && n > max) max = n;
    }
    return max;
  }

  async insert(transaction: Transaction): Promise<void> {
    const key = transaction.transactionId.padStart(TRAN_ID_LENGTH, '0');
    if (this.byId.has(key)) {
      throw new BillPaymentError('TRAN_ID_DUPLICATE');
    }
    this.byId.set(key, { ...transaction });
  }

  async findById(transactionId: string): Promise<Transaction | null> {
    const found = this.byId.get(transactionId.padStart(TRAN_ID_LENGTH, '0'));
    return found ? { ...found } : null;
  }
}

/** Build a fresh set of repositories seeded with demo data. */
export function createSeededRepositories(): Repositories {
  const accounts: Account[] = [
    {
      acctId: '00000000011',
      activeStatus: 'Y',
      currentBalance: Money.fromDecimal(1234.56),
      creditLimit: Money.fromDecimal(5000.0),
      cashCreditLimit: Money.fromDecimal(1000.0),
      openDate: '2020-01-15',
      expirationDate: '2027-01-15',
      reissueDate: '2024-01-15',
      currentCycleCredit: Money.zero(),
      currentCycleDebit: Money.fromDecimal(1234.56),
      addressZip: '94105',
      groupId: 'STANDARD',
    },
    {
      acctId: '00000000022',
      activeStatus: 'Y',
      currentBalance: Money.zero(),
      creditLimit: Money.fromDecimal(2500.0),
      cashCreditLimit: Money.fromDecimal(500.0),
      openDate: '2021-06-01',
      expirationDate: '2028-06-01',
      reissueDate: '2025-06-01',
      currentCycleCredit: Money.zero(),
      currentCycleDebit: Money.zero(),
      addressZip: '10001',
      groupId: 'STANDARD',
    },
  ];

  const cardXrefs: CardXref[] = [
    { cardNumber: '4111111111111111', customerId: '000000001', acctId: '00000000011' },
    { cardNumber: '4222222222222222', customerId: '000000002', acctId: '00000000022' },
  ];

  const transactions: Transaction[] = [
    {
      transactionId: '0000000000000100',
      typeCode: '01',
      categoryCode: 1,
      source: 'POS TERM',
      description: 'GROCERY STORE PURCHASE',
      amount: Money.fromDecimal(1234.56),
      merchantId: '000123456',
      merchantName: 'WHOLE FOODS',
      merchantCity: 'SAN FRANCISCO',
      merchantZip: '94105',
      cardNumber: '4111111111111111',
      originTimestamp: '2026-06-01 10:00:00.000000',
      processTimestamp: '2026-06-01 10:00:00.000000',
    },
  ];

  return {
    accounts: new InMemoryAccountRepository(accounts),
    cardXrefs: new InMemoryCardXrefRepository(cardXrefs),
    transactions: new InMemoryTransactionRepository(transactions),
  };
}
