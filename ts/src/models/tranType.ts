import { RecordLayout } from './layout';

/**
 * TRAN-TYPE-RECORD (copybook CVTRA03Y, LRECL 60, dataset TRANTYPE).
 *
 * off  len  pic        field
 *   0    2  X(02)      TRAN-TYPE
 *   2   50  X(50)      TRAN-TYPE-DESC
 *  52    8  X(08)      FILLER
 */
export interface TranTypeRecord {
  tranType: string;
  tranTypeDesc: string;
}

export const TRAN_TYPE_RECORD_LENGTH = 60;

export const TRAN_TYPE_LAYOUT: RecordLayout<TranTypeRecord> = {
  copybook: 'CVTRA03Y',
  length: TRAN_TYPE_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'tranType', length: 2 },
    { kind: 'alphanumeric', name: 'tranTypeDesc', length: 50 },
    { kind: 'filler', length: 8 },
  ],
  fromFields: (v) => ({
    tranType: v.tranType as string,
    tranTypeDesc: v.tranTypeDesc as string,
  }),
  toFields: (r) => ({ ...r }),
};
