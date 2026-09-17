import { TRANSACTION_LAYOUT, TransactionRecord } from '../models/transaction';
import { KsdsFile } from './ksds';

/**
 * TRANSACT (MFE.CARDDEMO.TRANSACT.VSAM.KSDS, CICS file TRANSACT) keyed on
 * TRAN-ID(16), with the TRANIDX alternate index on TRAN-CARD-NUM(16).
 */
export function transactionKey(tranId: string): string {
  return tranId.trim().padEnd(16, ' ');
}

export const TRANIDX = 'TRANIDX';

export class TransactionRepository {
  readonly file = new KsdsFile<TransactionRecord>({
    name: 'TRANSACT',
    layout: TRANSACTION_LAYOUT,
    keyOf: (record) => transactionKey(record.tranId),
    alternateKeys: {
      [TRANIDX]: (record) => record.tranCardNum.padEnd(16, ' '),
    },
  });

  load(records: Iterable<TransactionRecord>): void {
    this.file.load(records);
  }

  read(tranId: string): TransactionRecord {
    return this.file.read(transactionKey(tranId));
  }

  tryRead(tranId: string): TransactionRecord | undefined {
    return this.file.tryRead(transactionKey(tranId));
  }

  readAll(): TransactionRecord[] {
    return this.file.readAll();
  }

  readByCardNum(cardNum: string): TransactionRecord[] {
    return this.file.readByAlternate(TRANIDX, cardNum.trim().padEnd(16, ' '));
  }

  write(record: TransactionRecord): void {
    this.file.write(record);
  }

  rewrite(record: TransactionRecord): void {
    this.file.rewrite(record);
  }

  delete(tranId: string): void {
    this.file.delete(transactionKey(tranId));
  }

  startBrowse(tranId?: string) {
    return this.file.startBrowse(tranId === undefined ? undefined : transactionKey(tranId));
  }
}
