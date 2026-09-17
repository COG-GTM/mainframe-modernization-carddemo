import { TRAN_TYPE_LAYOUT, TranTypeRecord } from '../models/tranType';
import { KsdsFile } from './ksds';

/** TRANTYPE (MFE.CARDDEMO.TRANTYPE, CICS file TRANTYPE) keyed on TRAN-TYPE(2). */
export function tranTypeKey(tranType: string): string {
  return tranType.trim().padEnd(2, ' ');
}

export class TranTypeRepository {
  readonly file = new KsdsFile<TranTypeRecord>({
    name: 'TRANTYPE',
    layout: TRAN_TYPE_LAYOUT,
    keyOf: (record) => tranTypeKey(record.tranType),
  });

  load(records: Iterable<TranTypeRecord>): void {
    this.file.load(records);
  }

  read(tranType: string): TranTypeRecord {
    return this.file.read(tranTypeKey(tranType));
  }

  tryRead(tranType: string): TranTypeRecord | undefined {
    return this.file.tryRead(tranTypeKey(tranType));
  }

  readAll(): TranTypeRecord[] {
    return this.file.readAll();
  }

  write(record: TranTypeRecord): void {
    this.file.write(record);
  }

  rewrite(record: TranTypeRecord): void {
    this.file.rewrite(record);
  }
}
