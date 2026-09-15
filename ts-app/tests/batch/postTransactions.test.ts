import { Decimal } from 'decimal.js';
import { describe, expect, it } from 'vitest';
import { InMemoryKeyedRepository, type CardDemoRepositories } from '../../src/data/repositories.js';
import {
  tranCatKey,
  type AccountRecord,
  type CardXrefRecord,
  type DailyTransactionRecord,
  type TranCatBalanceRecord,
  type TransactionRecord,
} from '../../src/domain/index.js';
import { InMemoryLineWriter } from '../../src/batch/sequentialWriter.js';
import {
  runPostTransactions,
  validateTransaction,
  VALIDATION_MESSAGES,
  VALIDATION_REASONS,
} from '../../src/batch/postTransactions.js';

const CARD_NUM = '4111111111111111';
const ACCT_ID = '00000000011';

function dailyTransaction(overrides: Partial<DailyTransactionRecord> = {}): DailyTransactionRecord {
  return {
    dalytranId: '0000000000000001',
    dalytranTypeCd: '01',
    dalytranCatCd: '0005',
    dalytranSource: 'POS TERM',
    dalytranDesc: 'GROCERIES',
    dalytranAmt: new Decimal('100.50'),
    dalytranMerchantId: '000123456',
    dalytranMerchantName: 'CORNER STORE',
    dalytranMerchantCity: 'SEATTLE',
    dalytranMerchantZip: '98101',
    dalytranCardNum: CARD_NUM,
    dalytranOrigTs: '2022-01-01-10.00.00.000000',
    dalytranProcTs: '',
    ...overrides,
  };
}

function account(overrides: Partial<AccountRecord> = {}): AccountRecord {
  return {
    acctId: ACCT_ID,
    acctActiveStatus: 'Y',
    acctCurrBal: new Decimal('1000.00'),
    acctCreditLimit: new Decimal('5000.00'),
    acctCashCreditLimit: new Decimal('500.00'),
    acctOpenDate: '2020-01-01',
    acctExpiraionDate: '2030-12-31',
    acctReissueDate: '2024-01-01',
    acctCurrCycCredit: new Decimal('200.00'),
    acctCurrCycDebit: new Decimal('50.00'),
    acctAddrZip: '98101',
    acctGroupId: 'GRP01',
    ...overrides,
  };
}

const xref: CardXrefRecord = {
  xrefCardNum: CARD_NUM,
  xrefCustId: '000000001',
  xrefAcctId: ACCT_ID,
};

interface RepositoryOverrides {
  accounts?: AccountRecord[];
  xrefs?: CardXrefRecord[];
  balances?: TranCatBalanceRecord[];
  dailyTransactions?: DailyTransactionRecord[];
}

function repositories(overrides: RepositoryOverrides = {}): CardDemoRepositories {
  const xrefs = overrides.xrefs ?? [xref];
  const accounts = overrides.accounts ?? [account()];
  const balances = overrides.balances ?? [];

  return {
    users: new InMemoryKeyedRepository((record) => record.secUsrId),
    accounts: new InMemoryKeyedRepository<AccountRecord>((record) => record.acctId, accounts),
    cards: new InMemoryKeyedRepository((record) => record.cardNum),
    cardXrefs: new InMemoryKeyedRepository<CardXrefRecord>((record) => record.xrefCardNum, xrefs),
    cardXrefsByAccount: new InMemoryKeyedRepository<CardXrefRecord>(
      (record) => record.xrefAcctId,
      xrefs,
    ),
    customers: new InMemoryKeyedRepository((record) => record.custId),
    tranCatBalances: new InMemoryKeyedRepository<TranCatBalanceRecord>(
      (record) => tranCatKey(record.trancatAcctId, record.trancatTypeCd, record.trancatCd),
      balances,
    ),
    transactions: new InMemoryKeyedRepository<TransactionRecord>((record) => record.tranId),
    dailyTransactions: overrides.dailyTransactions ?? [dailyTransaction()],
  };
}

function run(
  overrides: RepositoryOverrides = {},
): ReturnType<typeof runPostTransactions> & { rejectWriter: InMemoryLineWriter } {
  const rejectWriter = new InMemoryLineWriter();
  const summary = runPostTransactions({
    repositories: repositories(overrides),
    rejectWriter,
    now: () => new Date(2022, 0, 2, 3, 4, 5, 670),
  });
  return { ...summary, rejectWriter };
}

describe('1500-VALIDATE-TRAN', () => {
  it('rejects an unknown card number with reason 100', () => {
    const result = validateTransaction(
      dailyTransaction({ dalytranCardNum: '9999999999999999' }),
      repositories(),
    );

    expect(result.reason).toBe(VALIDATION_REASONS.invalidCardNumber);
    expect(result.description).toBe('INVALID CARD NUMBER FOUND');
  });

  it('rejects a missing account with reason 101', () => {
    const result = validateTransaction(dailyTransaction(), repositories({ accounts: [] }));

    expect(result.reason).toBe(VALIDATION_REASONS.accountNotFound);
    expect(result.description).toBe('ACCOUNT RECORD NOT FOUND');
  });

  it('rejects an over-limit transaction with reason 102', () => {
    // WS-TEMP-BAL = 200.00 - 50.00 + 4850.01 = 5000.01 > ACCT-CREDIT-LIMIT
    const result = validateTransaction(
      dailyTransaction({ dalytranAmt: new Decimal('4850.01') }),
      repositories(),
    );

    expect(result.reason).toBe(VALIDATION_REASONS.overlimit);
    expect(result.description).toBe('OVERLIMIT TRANSACTION');
  });

  it('passes on the over-limit boundary where ACCT-CREDIT-LIMIT equals WS-TEMP-BAL', () => {
    const result = validateTransaction(
      dailyTransaction({ dalytranAmt: new Decimal('4850.00') }),
      repositories(),
    );

    expect(result.reason).toBe(VALIDATION_REASONS.ok);
  });

  it('rejects a transaction received after expiration with reason 103', () => {
    const result = validateTransaction(
      dailyTransaction({ dalytranOrigTs: '2031-01-01-10.00.00.000000' }),
      repositories({ accounts: [account({ acctExpiraionDate: '2030-12-31' })] }),
    );

    expect(result.reason).toBe(VALIDATION_REASONS.expiredAccount);
    expect(result.description).toBe('TRANSACTION RECEIVED AFTER ACCT EXPIRATION');
  });

  it('passes when the expiration date equals the origination date', () => {
    const result = validateTransaction(
      dailyTransaction({ dalytranOrigTs: '2030-12-31-10.00.00.000000' }),
      repositories(),
    );

    expect(result.reason).toBe(VALIDATION_REASONS.ok);
  });
});

describe('2500-WRITE-REJECT-REC', () => {
  it('writes the daily record followed by the reason code and message', () => {
    const summary = run({
      dailyTransactions: [dailyTransaction({ dalytranCardNum: '9999999999999999' })],
    });

    expect(summary.rejects).toHaveLength(1);
    const reject = summary.rejects[0] as string;
    expect(reject).toHaveLength(430);
    expect(reject.slice(0, 16)).toBe('0000000000000001');
    expect(reject.slice(350, 354)).toBe('0100');
    expect(reject.slice(354).trimEnd()).toBe(
      VALIDATION_MESSAGES[VALIDATION_REASONS.invalidCardNumber],
    );
    expect(reject.slice(354)).toHaveLength(76);
  });

  it('counts rejects and reports COBOL RETURN-CODE 4', () => {
    const summary = run({
      dailyTransactions: [
        dailyTransaction(),
        dailyTransaction({ dalytranId: '2', dalytranCardNum: '9999999999999999' }),
        dailyTransaction({ dalytranId: '3', dalytranAmt: new Decimal('99999.00') }),
      ],
    });

    expect(summary.transactionCount).toBe(3);
    expect(summary.rejectCount).toBe(2);
    expect(summary.returnCode).toBe(4);
  });

  it('reports return code 0 when nothing is rejected', () => {
    const summary = run();

    expect(summary.transactionCount).toBe(1);
    expect(summary.rejectCount).toBe(0);
    expect(summary.returnCode).toBe(0);
    expect(summary.rejects).toHaveLength(0);
  });
});

describe('2700-UPDATE-TCATBAL', () => {
  it('creates the category balance record when the key is missing', () => {
    const summary = run();

    const balance = summary.repositories.tranCatBalances.read(tranCatKey(ACCT_ID, '01', '0005'));
    expect(balance).toBeDefined();
    expect(balance?.trancatAcctId).toBe(ACCT_ID);
    expect(balance?.trancatTypeCd).toBe('01');
    expect(balance?.trancatCd).toBe('0005');
    expect(balance?.tranCatBal.toFixed(2)).toBe('100.50');
  });

  it('adds the amount to an existing category balance', () => {
    const summary = run({
      balances: [
        {
          trancatAcctId: ACCT_ID,
          trancatTypeCd: '01',
          trancatCd: '0005',
          tranCatBal: new Decimal('10.01'),
        },
      ],
      dailyTransactions: [dailyTransaction({ dalytranAmt: new Decimal('0.02') })],
    });

    const balance = summary.repositories.tranCatBalances.read(tranCatKey(ACCT_ID, '01', '0005'));
    expect(balance?.tranCatBal.toFixed(2)).toBe('10.03');
  });
});

describe('2800-UPDATE-ACCOUNT-REC', () => {
  it('adds a positive amount to the balance and the cycle credit', () => {
    const summary = run({
      dailyTransactions: [dailyTransaction({ dalytranAmt: new Decimal('0.10') })],
    });

    const updated = summary.repositories.accounts.read(ACCT_ID) as AccountRecord;
    expect(updated.acctCurrBal.toFixed(2)).toBe('1000.10');
    expect(updated.acctCurrCycCredit.toFixed(2)).toBe('200.10');
    expect(updated.acctCurrCycDebit.toFixed(2)).toBe('50.00');
  });

  it('adds a negative amount to the balance and the cycle debit', () => {
    const summary = run({
      dailyTransactions: [dailyTransaction({ dalytranAmt: new Decimal('-25.75') })],
    });

    const updated = summary.repositories.accounts.read(ACCT_ID) as AccountRecord;
    expect(updated.acctCurrBal.toFixed(2)).toBe('974.25');
    expect(updated.acctCurrCycCredit.toFixed(2)).toBe('200.00');
    expect(updated.acctCurrCycDebit.toFixed(2)).toBe('24.25');
  });

  it('keeps cent precision across many postings', () => {
    const summary = run({
      accounts: [
        account({
          acctCurrBal: new Decimal('0'),
          acctCurrCycCredit: new Decimal('0'),
          acctCurrCycDebit: new Decimal('0'),
        }),
      ],
      dailyTransactions: Array.from({ length: 10 }, (_unused, index) =>
        dailyTransaction({
          dalytranId: String(index).padStart(16, '0'),
          dalytranAmt: new Decimal('0.07'),
        }),
      ),
    });

    const updated = summary.repositories.accounts.read(ACCT_ID) as AccountRecord;
    expect(updated.acctCurrBal.toFixed(2)).toBe('0.70');
    expect(updated.acctCurrCycCredit.toFixed(2)).toBe('0.70');
  });
});

describe('2900-WRITE-TRANSACTION-FILE', () => {
  it('copies the daily transaction fields and stamps TRAN-PROC-TS', () => {
    const summary = run();

    const posted = summary.repositories.transactions.read('0000000000000001');
    expect(posted?.tranCardNum).toBe(CARD_NUM);
    expect(posted?.tranTypeCd).toBe('01');
    expect(posted?.tranCatCd).toBe('0005');
    expect(posted?.tranAmt.toFixed(2)).toBe('100.50');
    expect(posted?.tranOrigTs).toBe('2022-01-01-10.00.00.000000');
    expect(posted?.tranProcTs).toBe('2022-01-02-03.04.05.670000');
  });

  it('writes fixed-width transaction lines to the sequential sink', () => {
    const transactionWriter = new InMemoryLineWriter();
    runPostTransactions({
      repositories: repositories(),
      rejectWriter: new InMemoryLineWriter(),
      transactionWriter,
      now: () => new Date(2022, 0, 2, 3, 4, 5, 670),
    });

    const lines = transactionWriter.records();
    expect(lines).toHaveLength(1);
    expect(lines[0]).toHaveLength(350);
  });

  it('does not post rejected transactions', () => {
    const summary = run({ accounts: [] });

    expect(summary.repositories.transactions.values()).toHaveLength(0);
  });
});
