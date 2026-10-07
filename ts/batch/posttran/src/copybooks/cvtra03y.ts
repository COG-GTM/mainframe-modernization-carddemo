import { defineLayout } from './layout.js';

/**
 * CVTRA03Y – TRAN-TYPE-RECORD (TRANTYPE KSDS, RECLN 60, key TRAN-TYPE).
 * Not read by CBTRN02C; ported for the data layer / reporting programs.
 */
export interface TranTypeRecord {
  /** TRAN-TYPE PIC X(02) – record key */
  tranType: string;
  /** TRAN-TYPE-DESC PIC X(50) */
  tranTypeDesc: string;
}

export const TRAN_TYPE_LAYOUT = defineLayout<TranTypeRecord>('TRAN-TYPE-RECORD (CVTRA03Y)', [
  { kind: 'alpha', name: 'tranType', length: 2 },
  { kind: 'alpha', name: 'tranTypeDesc', length: 50 },
  { kind: 'filler', length: 8 },
]);

export const tranTypeKey = (r: TranTypeRecord): string => r.tranType.padEnd(2, ' ');
