import type { Decimal } from 'decimal.js';
import {
  ACCOUNT_LAYOUT,
  type AccountRecord,
  CARD_XREF_LAYOUT,
  type CardXrefRecord,
  compareAlphanumeric,
  DAILY_TRAN_LAYOUT,
  type DailyTranRecord,
  TRAN_CAT_BAL_LAYOUT,
  TRAN_LAYOUT,
  type TranCatBalRecord,
  tranCatKeyOf,
  type TranRecord,
  toPic,
} from '../copybooks/index.js';
import type { FileStatus, KeyedFile, SequentialInputFile, SequentialOutputFile } from '../dal/types.js';
import { RejectReason, RejectReasonDesc, type RejectRecord } from './records.js';

/** The six DD statements of POSTTRAN STEP15, bound to data-access implementations. */
export interface PostTranFiles {
  /** DALYTRAN – daily transactions (sequential input) */
  dalytran: SequentialInputFile<DailyTranRecord>;
  /** TRANFILE – transaction master KSDS (random, output records) */
  tranfile: KeyedFile<TranRecord>;
  /** XREFFILE – card cross-reference KSDS (random read) */
  xreffile: KeyedFile<CardXrefRecord>;
  /** DALYREJS – rejected transactions (sequential output) */
  dalyrejs: SequentialOutputFile<RejectRecord>;
  /** ACCTFILE – account master KSDS (random read/rewrite) */
  acctfile: KeyedFile<AccountRecord>;
  /** TCATBALF – transaction category balance KSDS (random read/write/rewrite) */
  tcatbalf: KeyedFile<TranCatBalRecord>;
}

export interface PostTranOptions {
  /** FUNCTION CURRENT-DATE source; inject a fixed clock for deterministic output. */
  clock?: () => Date;
  /** DISPLAY sink (SYSOUT). Defaults to console.log. */
  display?: (line: string) => void;
}

export interface PostTranResult {
  /** RETURN-CODE: 0, or 4 when any transaction was rejected. */
  returnCode: 0 | 4;
  transactionCount: number;
  rejectCount: number;
}

/** Raised by 9999-ABEND-PROGRAM (CALL 'CEE3ABD' with ABCODE 999). */
export class AbendError extends Error {
  readonly abendCode = 999;
  constructor(readonly reason: string, readonly fileStatus?: FileStatus) {
    super(`ABEND U0999: ${reason}${fileStatus ? ` (FILE STATUS ${fileStatus})` : ''}`);
  }
}

const pad9 = (n: number): string => String(n).padStart(9, '0');
const two = (n: number): string => String(n).padStart(2, '0');

/** Z-GET-DB2-FORMAT-TIMESTAMP: YYYY-MM-DD-HH.MM.SS.hh0000 from FUNCTION CURRENT-DATE (local time). */
export function db2FormatTimestamp(now: Date): string {
  const hundredths = Math.floor(now.getMilliseconds() / 10);
  return (
    `${now.getFullYear()}-${two(now.getMonth() + 1)}-${two(now.getDate())}-` +
    `${two(now.getHours())}.${two(now.getMinutes())}.${two(now.getSeconds())}.${two(hundredths)}0000`
  );
}

/** 9910-DISPLAY-IO-STATUS text for a two-character FILE STATUS. */
export function formatIoStatus(status: string): string {
  const stat1 = status.charAt(0);
  const stat2 = status.charAt(1);
  if (!/^[0-9]{2}$/.test(status) || stat1 === '9') {
    const binary = String(stat2.charCodeAt(0) || 0).padStart(3, '0').slice(-3);
    return `FILE STATUS IS: NNNN${stat1}${binary}`;
  }
  return `FILE STATUS IS: NNNN00${status}`;
}

/**
 * CBTRN02C – post the daily transaction file.
 *
 * Each method corresponds to one COBOL paragraph (named in its doc comment);
 * WORKING-STORAGE lives on the instance so READ INTO / INITIALIZE residue
 * behaves the same way as in the original program.
 */
export class Cbtrn02c {
  private readonly clock: () => Date;
  private readonly display: (line: string) => void;

  // ---- WORKING-STORAGE ---------------------------------------------------
  private dalytranRecord: DailyTranRecord = DAILY_TRAN_LAYOUT.empty();
  private tranRecord: TranRecord = TRAN_LAYOUT.empty();
  private cardXrefRecord: CardXrefRecord = CARD_XREF_LAYOUT.empty();
  private accountRecord: AccountRecord = ACCOUNT_LAYOUT.empty();
  private tranCatBalRecord: TranCatBalRecord = TRAN_CAT_BAL_LAYOUT.empty();
  private endOfFile: 'Y' | 'N' = 'N';
  private wsValidationFailReason = 0;
  private wsValidationFailReasonDesc = '';
  private wsTransactionCount = 0;
  private wsRejectCount = 0;
  private wsCreateTrancatRec: 'Y' | 'N' = 'N';

  constructor(
    private readonly files: PostTranFiles,
    options: PostTranOptions = {},
  ) {
    this.clock = options.clock ?? (() => new Date());
    this.display = options.display ?? ((line) => console.log(line));
  }

  /** PROCEDURE DIVISION mainline. */
  async run(): Promise<PostTranResult> {
    this.display('START OF EXECUTION OF PROGRAM CBTRN02C');
    await this.openFile(this.files.dalytran, 'ERROR OPENING DALYTRAN'); // 0000-DALYTRAN-OPEN
    await this.openFile(this.files.tranfile, 'ERROR OPENING TRANSACTION FILE'); // 0100-TRANFILE-OPEN
    await this.openFile(this.files.xreffile, 'ERROR OPENING CROSS REF FILE'); // 0200-XREFFILE-OPEN
    await this.openFile(this.files.dalyrejs, 'ERROR OPENING DALY REJECTS FILE'); // 0300-DALYREJS-OPEN
    await this.openFile(this.files.acctfile, 'ERROR OPENING ACCOUNT MASTER FILE'); // 0400-ACCTFILE-OPEN
    await this.openFile(this.files.tcatbalf, 'ERROR OPENING TRANSACTION BALANCE FILE'); // 0500-TCATBALF-OPEN

    while (this.endOfFile !== 'Y') {
      if (this.endOfFile === 'N') {
        await this.dalytranGetNext();
        if (this.endOfFile === 'N') {
          this.wsTransactionCount += 1;
          this.wsValidationFailReason = 0;
          this.wsValidationFailReasonDesc = '';
          await this.validateTran();
          if (this.wsValidationFailReason === 0) {
            await this.postTransaction();
          } else {
            this.wsRejectCount += 1;
            await this.writeRejectRec();
          }
        }
      }
    }

    await this.closeFile(this.files.dalytran, 'ERROR CLOSING DALYTRAN FILE'); // 9000-DALYTRAN-CLOSE
    await this.closeFile(this.files.tranfile, 'ERROR CLOSING TRANSACTION FILE'); // 9100-TRANFILE-CLOSE
    await this.closeFile(this.files.xreffile, 'ERROR CLOSING CROSS REF FILE'); // 9200-XREFFILE-CLOSE
    await this.closeFile(this.files.dalyrejs, 'ERROR CLOSING DAILY REJECTS FILE'); // 9300-DALYREJS-CLOSE
    await this.closeFile(this.files.acctfile, 'ERROR CLOSING ACCOUNT FILE'); // 9400-ACCTFILE-CLOSE
    await this.closeFile(this.files.tcatbalf, 'ERROR CLOSING TRANSACTION BALANCE FILE'); // 9500-TCATBALF-CLOSE
    this.display(`TRANSACTIONS PROCESSED :${pad9(this.wsTransactionCount)}`);
    this.display(`TRANSACTIONS REJECTED  :${pad9(this.wsRejectCount)}`);
    const returnCode = this.wsRejectCount > 0 ? 4 : 0;
    this.display('END OF EXECUTION OF PROGRAM CBTRN02C');
    return { returnCode, transactionCount: this.wsTransactionCount, rejectCount: this.wsRejectCount };
  }

  /** 0000- … 0500-xxx-OPEN: OPEN, abend unless FILE STATUS '00'. */
  private async openFile(file: { open(): Promise<FileStatus> }, message: string): Promise<void> {
    const status = await file.open();
    if (status !== '00') this.abend(message, status);
  }

  /** 9000- … 9500-xxx-CLOSE: CLOSE, abend unless FILE STATUS '00'. */
  private async closeFile(file: { close(): Promise<FileStatus> }, message: string): Promise<void> {
    const status = await file.close();
    if (status !== '00') this.abend(message, status);
  }

  /** 1000-DALYTRAN-GET-NEXT */
  private async dalytranGetNext(): Promise<void> {
    const { status, record } = await this.files.dalytran.read();
    if (status === '00' && record) {
      this.dalytranRecord = record;
    } else if (status === '10') {
      this.endOfFile = 'Y';
    } else {
      this.abend('ERROR READING DALYTRAN FILE', status);
    }
  }

  /** 1500-VALIDATE-TRAN */
  private async validateTran(): Promise<void> {
    await this.lookupXref();
    if (this.wsValidationFailReason === 0) {
      await this.lookupAcct();
    }
  }

  /** 1500-A-LOOKUP-XREF */
  private async lookupXref(): Promise<void> {
    const { status, record } = await this.files.xreffile.read(this.dalytranRecord.dalytranCardNum.padEnd(16, ' '));
    if (status === '00' && record) {
      this.cardXrefRecord = record;
    } else {
      this.reject(RejectReason.INVALID_CARD_NUMBER);
    }
  }

  /** 1500-B-LOOKUP-ACCT */
  private async lookupAcct(): Promise<void> {
    const { status, record } = await this.files.acctfile.read(this.cardXrefRecord.xrefAcctId);
    if (status !== '00' || !record) {
      this.reject(RejectReason.ACCOUNT_NOT_FOUND);
      return;
    }
    this.accountRecord = record;
    // COMPUTE WS-TEMP-BAL (PIC S9(09)V99) = CYC-CREDIT - CYC-DEBIT + DALYTRAN-AMT
    const wsTempBal: Decimal = toPic(
      this.accountRecord.acctCurrCycCredit
        .minus(this.accountRecord.acctCurrCycDebit)
        .plus(this.dalytranRecord.dalytranAmt),
      9,
      2,
    );
    if (!this.accountRecord.acctCreditLimit.greaterThanOrEqualTo(wsTempBal)) {
      this.reject(RejectReason.OVERLIMIT);
    }
    // Both checks run; a later failure overwrites an earlier one (103 wins over 102).
    if (compareAlphanumeric(this.accountRecord.acctExpirationDate, this.dalytranRecord.dalytranOrigTs.slice(0, 10)) < 0) {
      this.reject(RejectReason.ACCOUNT_EXPIRED);
    }
  }

  /** 2000-POST-TRANSACTION */
  private async postTransaction(): Promise<void> {
    const d = this.dalytranRecord;
    this.tranRecord = {
      ...this.tranRecord,
      tranId: d.dalytranId,
      tranTypeCd: d.dalytranTypeCd,
      tranCatCd: d.dalytranCatCd,
      tranSource: d.dalytranSource,
      tranDesc: d.dalytranDesc,
      tranAmt: d.dalytranAmt,
      tranMerchantId: d.dalytranMerchantId,
      tranMerchantName: d.dalytranMerchantName,
      tranMerchantCity: d.dalytranMerchantCity,
      tranMerchantZip: d.dalytranMerchantZip,
      tranCardNum: d.dalytranCardNum,
      tranOrigTs: d.dalytranOrigTs,
      tranProcTs: db2FormatTimestamp(this.clock()), // Z-GET-DB2-FORMAT-TIMESTAMP
    };
    await this.updateTcatbal();
    await this.updateAccountRec();
    await this.writeTransactionFile();
  }

  /** 2500-WRITE-REJECT-REC */
  private async writeRejectRec(): Promise<void> {
    const status = await this.files.dalyrejs.write({
      rejectTranData: DAILY_TRAN_LAYOUT.format(this.dalytranRecord),
      validationFailReason: String(this.wsValidationFailReason).padStart(4, '0'),
      validationFailReasonDesc: this.wsValidationFailReasonDesc,
    });
    if (status !== '00') this.abend('ERROR WRITING TO REJECTS FILE', status);
  }

  /** 2700-UPDATE-TCATBAL */
  private async updateTcatbal(): Promise<void> {
    const key = tranCatKeyOf(
      this.cardXrefRecord.xrefAcctId,
      this.dalytranRecord.dalytranTypeCd,
      this.dalytranRecord.dalytranCatCd,
    );
    this.wsCreateTrancatRec = 'N';
    const { status, record } = await this.files.tcatbalf.read(key);
    if (status === '23') {
      this.display(`TCATBAL record not found for key : ${key}.. Creating.`);
      this.wsCreateTrancatRec = 'Y';
    } else if (status === '00' && record) {
      this.tranCatBalRecord = record;
    } else {
      this.abend('ERROR READING TRANSACTION BALANCE FILE', status);
    }

    if (this.wsCreateTrancatRec === 'Y') {
      await this.createTcatbalRec();
    } else {
      await this.updateTcatbalRec();
    }
  }

  /** 2700-A-CREATE-TCATBAL-REC */
  private async createTcatbalRec(): Promise<void> {
    const rec = TRAN_CAT_BAL_LAYOUT.initialize(this.tranCatBalRecord);
    rec.trancatAcctId = this.cardXrefRecord.xrefAcctId;
    rec.trancatTypeCd = this.dalytranRecord.dalytranTypeCd;
    rec.trancatCd = this.dalytranRecord.dalytranCatCd;
    rec.tranCatBal = toPic(rec.tranCatBal.plus(this.dalytranRecord.dalytranAmt), 9, 2);
    this.tranCatBalRecord = rec;
    const status = await this.files.tcatbalf.write(rec);
    if (status !== '00') this.abend('ERROR WRITING TRANSACTION BALANCE FILE', status);
  }

  /** 2700-B-UPDATE-TCATBAL-REC */
  private async updateTcatbalRec(): Promise<void> {
    this.tranCatBalRecord = {
      ...this.tranCatBalRecord,
      tranCatBal: toPic(this.tranCatBalRecord.tranCatBal.plus(this.dalytranRecord.dalytranAmt), 9, 2),
    };
    const status = await this.files.tcatbalf.rewrite(this.tranCatBalRecord);
    if (status !== '00') this.abend('ERROR REWRITING TRANSACTION BALANCE FILE', status);
  }

  /** 2800-UPDATE-ACCOUNT-REC */
  private async updateAccountRec(): Promise<void> {
    const amt = this.dalytranRecord.dalytranAmt;
    const acct = { ...this.accountRecord };
    acct.acctCurrBal = toPic(acct.acctCurrBal.plus(amt), 10, 2);
    if (amt.greaterThanOrEqualTo(0)) {
      acct.acctCurrCycCredit = toPic(acct.acctCurrCycCredit.plus(amt), 10, 2);
    } else {
      acct.acctCurrCycDebit = toPic(acct.acctCurrCycDebit.plus(amt), 10, 2);
    }
    this.accountRecord = acct;
    const status = await this.files.acctfile.rewrite(acct);
    if (status === '23') {
      // INVALID KEY: reason is recorded but, as in the COBOL, the transaction is still written.
      this.reject(RejectReason.ACCOUNT_REWRITE_NOT_FOUND);
    }
  }

  /** 2900-WRITE-TRANSACTION-FILE */
  private async writeTransactionFile(): Promise<void> {
    const status = await this.files.tranfile.write(this.tranRecord);
    if (status !== '00') this.abend('ERROR WRITING TO TRANSACTION FILE', status);
  }

  private reject(code: keyof typeof RejectReasonDesc): void {
    this.wsValidationFailReason = code;
    this.wsValidationFailReasonDesc = RejectReasonDesc[code];
  }

  /** 9910-DISPLAY-IO-STATUS + 9999-ABEND-PROGRAM */
  private abend(message: string, status: FileStatus): never {
    this.display(message);
    this.display(formatIoStatus(status));
    this.display('ABENDING PROGRAM');
    throw new AbendError(message, status);
  }
}

export async function runCbtrn02c(files: PostTranFiles, options?: PostTranOptions): Promise<PostTranResult> {
  return new Cbtrn02c(files, options).run();
}
