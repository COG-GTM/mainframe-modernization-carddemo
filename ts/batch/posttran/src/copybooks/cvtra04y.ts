import { defineLayout } from './layout.js';

/**
 * CVTRA04Y – TRAN-CAT-RECORD (TRANCATG KSDS, RECLN 60, key TRAN-CAT-KEY).
 * Not read by CBTRN02C; ported for the data layer / reporting programs.
 */
export interface TranCatRecord {
  /** TRAN-CAT-KEY.TRAN-TYPE-CD PIC X(02) */
  tranTypeCd: string;
  /** TRAN-CAT-KEY.TRAN-CAT-CD PIC 9(04) */
  tranCatCd: string;
  /** TRAN-CAT-TYPE-DESC PIC X(50) */
  tranCatTypeDesc: string;
}

export const TRAN_CAT_LAYOUT = defineLayout<TranCatRecord>('TRAN-CAT-RECORD (CVTRA04Y)', [
  { kind: 'alpha', name: 'tranTypeCd', length: 2 },
  { kind: 'unsigned', name: 'tranCatCd', length: 4 },
  { kind: 'alpha', name: 'tranCatTypeDesc', length: 50 },
  { kind: 'filler', length: 4 },
]);

export const tranCatKey = (r: TranCatRecord): string => r.tranTypeCd.padEnd(2, ' ') + r.tranCatCd.padStart(4, '0');
