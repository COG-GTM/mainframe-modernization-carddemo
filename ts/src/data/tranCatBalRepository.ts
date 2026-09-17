import {
  TRAN_CAT_BAL_LAYOUT,
  TranCatBalRecord,
  tranCatBalKey,
} from '../models/tranCategoryBalance';
import { KsdsFile } from './ksds';

/** TCATBALF (MFE.CARDDEMO.TCATBALF, CICS file TCATBALF) keyed on TRAN-CAT-KEY(17). */
export class TranCatBalRepository {
  readonly file = new KsdsFile<TranCatBalRecord>({
    name: 'TCATBALF',
    layout: TRAN_CAT_BAL_LAYOUT,
    keyOf: tranCatBalKey,
  });

  load(records: Iterable<TranCatBalRecord>): void {
    this.file.load(records);
  }

  read(acctId: number, tranTypeCd: string, tranCatCd: number): TranCatBalRecord {
    return this.file.read(
      tranCatBalKey({ trancatAcctId: acctId, trancatTypeCd: tranTypeCd, trancatCd: tranCatCd }),
    );
  }

  tryRead(acctId: number, tranTypeCd: string, tranCatCd: number): TranCatBalRecord | undefined {
    return this.file.tryRead(
      tranCatBalKey({ trancatAcctId: acctId, trancatTypeCd: tranTypeCd, trancatCd: tranCatCd }),
    );
  }

  readAll(): TranCatBalRecord[] {
    return this.file.readAll();
  }

  readByAccountId(acctId: number): TranCatBalRecord[] {
    return this.readAll().filter((record) => record.trancatAcctId === acctId);
  }

  write(record: TranCatBalRecord): void {
    this.file.write(record);
  }

  rewrite(record: TranCatBalRecord): void {
    this.file.rewrite(record);
  }

  /** CBTRN02C updates the balance when it exists and adds it otherwise. */
  put(record: TranCatBalRecord): void {
    this.file.put(record);
  }

  startBrowse(key?: string) {
    return this.file.startBrowse(key);
  }
}
