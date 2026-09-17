import { DAILY_TRANSACTION_LAYOUT, DailyTransactionRecord } from '../models/transaction';
import { KsdsFile } from './ksds';

/**
 * DALYTRAN (MFE.CARDDEMO.DALYTRAN.PS / .VSAM.KSDS) — read sequentially by the
 * posting job CBTRN02C and keyed on DALYTRAN-ID(16) when loaded into VSAM.
 */
export function dailyTransactionKey(tranId: string): string {
  return tranId.trim().padEnd(16, ' ');
}

export class DailyTransactionRepository {
  readonly file = new KsdsFile<DailyTransactionRecord>({
    name: 'DALYTRAN',
    layout: DAILY_TRANSACTION_LAYOUT,
    keyOf: (record) => dailyTransactionKey(record.tranId),
  });

  load(records: Iterable<DailyTransactionRecord>): void {
    this.file.load(records);
  }

  read(tranId: string): DailyTransactionRecord {
    return this.file.read(dailyTransactionKey(tranId));
  }

  tryRead(tranId: string): DailyTransactionRecord | undefined {
    return this.file.tryRead(dailyTransactionKey(tranId));
  }

  /** Sequential read of the whole dataset, in key order. */
  readAll(): DailyTransactionRecord[] {
    return this.file.readAll();
  }

  write(record: DailyTransactionRecord): void {
    this.file.write(record);
  }

  delete(tranId: string): void {
    this.file.delete(dailyTransactionKey(tranId));
  }

  startBrowse(tranId?: string) {
    return this.file.startBrowse(tranId === undefined ? undefined : dailyTransactionKey(tranId));
  }
}
