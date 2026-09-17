import { TRAN_CATEGORY_LAYOUT, TranCategoryRecord, tranCategoryKey } from '../models/tranCategory';
import { KsdsFile } from './ksds';

/** TRANCATG (MFE.CARDDEMO.TRANCATG, CICS file TRANCATG) keyed on TRAN-CAT-KEY(6). */
export class TranCategoryRepository {
  readonly file = new KsdsFile<TranCategoryRecord>({
    name: 'TRANCATG',
    layout: TRAN_CATEGORY_LAYOUT,
    keyOf: tranCategoryKey,
  });

  load(records: Iterable<TranCategoryRecord>): void {
    this.file.load(records);
  }

  read(tranTypeCd: string, tranCatCd: number): TranCategoryRecord {
    return this.file.read(tranCategoryKey({ tranTypeCd, tranCatCd }));
  }

  tryRead(tranTypeCd: string, tranCatCd: number): TranCategoryRecord | undefined {
    return this.file.tryRead(tranCategoryKey({ tranTypeCd, tranCatCd }));
  }

  readAll(): TranCategoryRecord[] {
    return this.file.readAll();
  }

  write(record: TranCategoryRecord): void {
    this.file.write(record);
  }

  rewrite(record: TranCategoryRecord): void {
    this.file.rewrite(record);
  }
}
