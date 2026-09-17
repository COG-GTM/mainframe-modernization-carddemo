import { RecordLayout } from './layout';

/**
 * TRAN-CAT-RECORD (copybook CVTRA04Y, LRECL 60, dataset TRANCATG).
 * The KSDS key is the 6 byte TRAN-CAT-KEY (type cd + cat cd).
 *
 * off  len  pic        field
 *   0    2  X(02)      TRAN-TYPE-CD \
 *   2    4  9(04)      TRAN-CAT-CD   > TRAN-CAT-KEY
 *   6   50  X(50)      TRAN-CAT-TYPE-DESC
 *  56    4  X(04)      FILLER
 */
export interface TranCategoryRecord {
  tranTypeCd: string;
  tranCatCd: number;
  tranCatTypeDesc: string;
}

export const TRAN_CATEGORY_RECORD_LENGTH = 60;

export const TRAN_CATEGORY_LAYOUT: RecordLayout<TranCategoryRecord> = {
  copybook: 'CVTRA04Y',
  length: TRAN_CATEGORY_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'tranTypeCd', length: 2 },
    { kind: 'unsigned', name: 'tranCatCd', length: 4 },
    { kind: 'alphanumeric', name: 'tranCatTypeDesc', length: 50 },
    { kind: 'filler', length: 4 },
  ],
  fromFields: (v) => ({
    tranTypeCd: v.tranTypeCd as string,
    tranCatCd: v.tranCatCd as number,
    tranCatTypeDesc: v.tranCatTypeDesc as string,
  }),
  toFields: (r) => ({ ...r }),
};

export function tranCategoryKey(record: { tranTypeCd: string; tranCatCd: number }): string {
  return record.tranTypeCd.padEnd(2, ' ') + String(record.tranCatCd).padStart(4, '0');
}
