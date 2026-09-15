import { Decimal } from 'decimal.js';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { alnum, digits, encodeRecord, type RecordLayout } from '../codec/fixedWidth.js';
import {
  createFileBackedRepositories,
  type FileRepositoryOptions,
} from '../data/fileRepositories.js';
import type { CardDemoRepositories, SequentialWriter } from '../data/repositories.js';
import {
  DAILY_TRANSACTION_LAYOUT,
  TRANSACTION_LAYOUT,
  tranCatKey,
  type AccountRecord,
  type CardXrefRecord,
  type DailyTransactionRecord,
  type TranCatBalanceRecord,
  type TransactionRecord,
} from '../domain/index.js';
import { FileLineWriter, InMemoryLineWriter } from './sequentialWriter.js';

/** WS-VALIDATION-FAIL-REASON values set by `1500-VALIDATE-TRAN`. */
export const VALIDATION_REASONS = {
  ok: 0,
  invalidCardNumber: 100,
  accountNotFound: 101,
  overlimit: 102,
  expiredAccount: 103,
} as const;

/** WS-VALIDATION-FAIL-REASON-DESC text, character-for-character from CBTRN02C. */
export const VALIDATION_MESSAGES: Record<number, string> = {
  [VALIDATION_REASONS.invalidCardNumber]: 'INVALID CARD NUMBER FOUND',
  [VALIDATION_REASONS.accountNotFound]: 'ACCOUNT RECORD NOT FOUND',
  [VALIDATION_REASONS.overlimit]: 'OVERLIMIT TRANSACTION',
  [VALIDATION_REASONS.expiredAccount]: 'TRANSACTION RECEIVED AFTER ACCT EXPIRATION',
};

/** WS-VALIDATION-TRAILER — `05 PIC 9(04)` plus `05 PIC X(76)`. */
export interface ValidationTrailer {
  wsValidationFailReason: string;
  wsValidationFailReasonDesc: string;
}

export const VALIDATION_TRAILER_LAYOUT: RecordLayout<ValidationTrailer> = {
  copybook: 'CBTRN02C-WS-VALIDATION-TRAILER',
  recordLength: 80,
  fields: [digits('wsValidationFailReason', 4), alnum('wsValidationFailReasonDesc', 76)],
};

/** REJECT-RECORD — the `DALYREJS` record (LRECL 430) written by 2500-WRITE-REJECT-REC. */
export interface RejectRecord {
  rejectTranData: string;
  validationTrailer: string;
}

export const REJECT_RECORD_LAYOUT: RecordLayout<RejectRecord> = {
  copybook: 'CBTRN02C-REJECT-RECORD',
  recordLength: 430,
  fields: [alnum('rejectTranData', 350), alnum('validationTrailer', 80)],
};

export type ValidationResult =
  | {
      ok: true;
      /** WS-VALIDATION-FAIL-REASON */
      reason: typeof VALIDATION_REASONS.ok;
      /** WS-VALIDATION-FAIL-REASON-DESC */
      description: string;
      xref: CardXrefRecord;
      account: AccountRecord;
    }
  | {
      ok: false;
      reason: number;
      description: string;
      xref: CardXrefRecord | undefined;
      account: AccountRecord | undefined;
    };

/**
 * `1500-VALIDATE-TRAN` — 1500-A-LOOKUP-XREF then, when the card resolves,
 * 1500-B-LOOKUP-ACCT (over-limit and expiration checks).
 */
export function validateTransaction(
  dailyTransaction: DailyTransactionRecord,
  repositories: CardDemoRepositories,
): ValidationResult {
  const xref = repositories.cardXrefs.read(dailyTransaction.dalytranCardNum);
  if (xref === undefined) {
    return {
      ok: false,
      reason: VALIDATION_REASONS.invalidCardNumber,
      description: VALIDATION_MESSAGES[VALIDATION_REASONS.invalidCardNumber] as string,
      xref: undefined,
      account: undefined,
    };
  }

  const account = repositories.accounts.read(xref.xrefAcctId);
  if (account === undefined) {
    return {
      ok: false,
      reason: VALIDATION_REASONS.accountNotFound,
      description: VALIDATION_MESSAGES[VALIDATION_REASONS.accountNotFound] as string,
      xref,
      account: undefined,
    };
  }

  let reason: number = VALIDATION_REASONS.ok;

  // WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
  const wsTempBal = account.acctCurrCycCredit
    .minus(account.acctCurrCycDebit)
    .plus(dailyTransaction.dalytranAmt);
  if (!account.acctCreditLimit.greaterThanOrEqualTo(wsTempBal)) {
    reason = VALIDATION_REASONS.overlimit;
  }

  // ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10)
  const expirationDate = account.acctExpiraionDate.padEnd(10, ' ');
  const originationDate = dailyTransaction.dalytranOrigTs.padEnd(10, ' ').slice(0, 10);
  if (!(expirationDate >= originationDate)) {
    reason = VALIDATION_REASONS.expiredAccount;
  }

  if (reason === VALIDATION_REASONS.ok) {
    return { ok: true, reason: VALIDATION_REASONS.ok, description: '', xref, account };
  }

  return {
    ok: false,
    reason,
    description: VALIDATION_MESSAGES[reason] as string,
    xref,
    account,
  };
}

/** `Z-GET-DB2-FORMAT-TIMESTAMP` — `EEEE-MM-DD-UU.MM.SS.HH0000`. */
export function db2FormatTimestamp(now: Date): string {
  const pad = (value: number, width: number): string => String(value).padStart(width, '0');
  const hundredths = Math.floor(now.getMilliseconds() / 10);
  return (
    `${pad(now.getFullYear(), 4)}-${pad(now.getMonth() + 1, 2)}-${pad(now.getDate(), 2)}-` +
    `${pad(now.getHours(), 2)}.${pad(now.getMinutes(), 2)}.${pad(now.getSeconds(), 2)}.` +
    `${pad(hundredths, 2)}0000`
  );
}

export interface PostTransactionsOptions extends FileRepositoryOptions {
  /** Defaults to `createFileBackedRepositories()` over the sample data files. */
  repositories?: CardDemoRepositories;
  /** DALYREJS. Defaults to an in-memory sink. */
  rejectWriter?: SequentialWriter<string>;
  /** TRANFILE, as fixed-width lines. Optional; the records are always kept in the repository. */
  transactionWriter?: SequentialWriter<string>;
  /** Clock used for TRAN-PROC-TS. */
  now?: () => Date;
}

export interface PostingSummary {
  /** WS-TRANSACTION-COUNT */
  transactionCount: number;
  /** WS-REJECT-COUNT */
  rejectCount: number;
  /** COBOL RETURN-CODE: 4 when any transaction was rejected. */
  returnCode: number;
  repositories: CardDemoRepositories;
  rejects: string[];
  transactions: TransactionRecord[];
}

/** `2500-WRITE-REJECT-REC` — the daily record followed by the validation trailer. */
function writeRejectRecord(
  dailyTransaction: DailyTransactionRecord,
  validation: ValidationResult,
  rejectWriter: SequentialWriter<string>,
): void {
  const validationTrailer = encodeRecord(VALIDATION_TRAILER_LAYOUT, {
    wsValidationFailReason: String(validation.reason),
    wsValidationFailReasonDesc: validation.description,
  });
  rejectWriter.write(
    encodeRecord(REJECT_RECORD_LAYOUT, {
      rejectTranData: encodeRecord(DAILY_TRANSACTION_LAYOUT, dailyTransaction),
      validationTrailer,
    }),
  );
}

/** `2700-UPDATE-TCATBAL` — create the category balance record when the key is absent. */
function updateTranCatBalance(
  dailyTransaction: DailyTransactionRecord,
  xref: CardXrefRecord,
  repositories: CardDemoRepositories,
): void {
  const key = tranCatKey(
    xref.xrefAcctId,
    dailyTransaction.dalytranTypeCd,
    dailyTransaction.dalytranCatCd,
  );
  const existing = repositories.tranCatBalances.read(key);

  const record: TranCatBalanceRecord =
    existing === undefined
      ? {
          // 2700-A-CREATE-TCATBAL-REC: INITIALIZE then ADD DALYTRAN-AMT TO TRAN-CAT-BAL
          trancatAcctId: xref.xrefAcctId,
          trancatTypeCd: dailyTransaction.dalytranTypeCd,
          trancatCd: dailyTransaction.dalytranCatCd,
          tranCatBal: new Decimal(0).plus(dailyTransaction.dalytranAmt),
        }
      : { ...existing, tranCatBal: existing.tranCatBal.plus(dailyTransaction.dalytranAmt) };

  repositories.tranCatBalances.write(key, record);
}

/** `2800-UPDATE-ACCOUNT-REC` — current balance plus the cycle credit/debit split. */
function updateAccountRecord(
  dailyTransaction: DailyTransactionRecord,
  account: AccountRecord,
  repositories: CardDemoRepositories,
): void {
  const amount = dailyTransaction.dalytranAmt;
  const updated: AccountRecord = {
    ...account,
    acctCurrBal: account.acctCurrBal.plus(amount),
    acctCurrCycCredit: amount.greaterThanOrEqualTo(0)
      ? account.acctCurrCycCredit.plus(amount)
      : account.acctCurrCycCredit,
    acctCurrCycDebit: amount.greaterThanOrEqualTo(0)
      ? account.acctCurrCycDebit
      : account.acctCurrCycDebit.plus(amount),
  };
  repositories.accounts.write(updated.acctId, updated);
}

/** `2000-POST-TRANSACTION` — builds TRAN-RECORD and performs 2700 / 2800 / 2900. */
function postTransaction(
  dailyTransaction: DailyTransactionRecord,
  xref: CardXrefRecord,
  account: AccountRecord,
  repositories: CardDemoRepositories,
  procTimestamp: string,
  transactionWriter: SequentialWriter<string> | undefined,
): TransactionRecord {
  const transaction: TransactionRecord = {
    tranId: dailyTransaction.dalytranId,
    tranTypeCd: dailyTransaction.dalytranTypeCd,
    tranCatCd: dailyTransaction.dalytranCatCd,
    tranSource: dailyTransaction.dalytranSource,
    tranDesc: dailyTransaction.dalytranDesc,
    tranAmt: dailyTransaction.dalytranAmt,
    tranMerchantId: dailyTransaction.dalytranMerchantId,
    tranMerchantName: dailyTransaction.dalytranMerchantName,
    tranMerchantCity: dailyTransaction.dalytranMerchantCity,
    tranMerchantZip: dailyTransaction.dalytranMerchantZip,
    tranCardNum: dailyTransaction.dalytranCardNum,
    tranOrigTs: dailyTransaction.dalytranOrigTs,
    tranProcTs: procTimestamp,
  };

  updateTranCatBalance(dailyTransaction, xref, repositories);
  updateAccountRecord(dailyTransaction, account, repositories);

  // 2900-WRITE-TRANSACTION-FILE
  repositories.transactions.write(transaction.tranId, transaction);
  transactionWriter?.write(encodeRecord(TRANSACTION_LAYOUT, transaction));

  return transaction;
}

/** `CBTRN02C` — posts the daily transaction file against the account master. */
export function runPostTransactions(options: PostTransactionsOptions = {}): PostingSummary {
  const repositories =
    options.repositories ??
    createFileBackedRepositories({
      ...(options.dataDir === undefined ? {} : { dataDir: options.dataDir }),
      ...(options.usrsecPath === undefined ? {} : { usrsecPath: options.usrsecPath }),
      ...(options.dailyTranPath === undefined ? {} : { dailyTranPath: options.dailyTranPath }),
    });
  const rejectWriter = options.rejectWriter ?? new InMemoryLineWriter();
  const now = options.now ?? ((): Date => new Date());

  let transactionCount = 0;
  let rejectCount = 0;
  const transactions: TransactionRecord[] = [];

  for (const dailyTransaction of repositories.dailyTransactions) {
    transactionCount += 1;
    const validation = validateTransaction(dailyTransaction, repositories);

    if (validation.ok) {
      transactions.push(
        postTransaction(
          dailyTransaction,
          validation.xref,
          validation.account,
          repositories,
          db2FormatTimestamp(now()),
          options.transactionWriter,
        ),
      );
    } else {
      rejectCount += 1;
      writeRejectRecord(dailyTransaction, validation, rejectWriter);
    }
  }

  return {
    transactionCount,
    rejectCount,
    returnCode: rejectCount > 0 ? 4 : 0,
    repositories,
    rejects: rejectWriter.records(),
    transactions,
  };
}

/** CLI wrapper for `npm run batch:post`. */
export function main(argv: readonly string[] = process.argv.slice(2)): number {
  const dailyTranPath = argv[0];
  const rejectPath = argv[1] ?? 'dalyrejs.txt';
  const rejectWriter = new FileLineWriter(rejectPath);

  console.log('START OF EXECUTION OF PROGRAM CBTRN02C');
  try {
    const summary = runPostTransactions({
      ...(dailyTranPath === undefined ? {} : { dailyTranPath }),
      rejectWriter,
    });
    console.log(`TRANSACTIONS PROCESSED :${String(summary.transactionCount).padStart(9, '0')}`);
    console.log(`TRANSACTIONS REJECTED  :${String(summary.rejectCount).padStart(9, '0')}`);
    console.log('END OF EXECUTION OF PROGRAM CBTRN02C');
    return summary.returnCode;
  } finally {
    rejectWriter.close();
  }
}

const entryPoint = process.argv[1];
if (entryPoint !== undefined && resolve(entryPoint) === fileURLToPath(import.meta.url)) {
  process.exit(main());
}
