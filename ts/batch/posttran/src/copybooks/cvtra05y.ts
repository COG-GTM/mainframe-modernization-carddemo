import type { Decimal } from 'decimal.js';
import { defineLayout } from './layout.js';

/** CVTRA05Y – TRAN-RECORD (transaction master TRANSACT KSDS, RECLN 350, key TRAN-ID). */
export interface TranRecord {
  /** TRAN-ID PIC X(16) – record key */
  tranId: string;
  /** TRAN-TYPE-CD PIC X(02) */
  tranTypeCd: string;
  /** TRAN-CAT-CD PIC 9(04) */
  tranCatCd: string;
  /** TRAN-SOURCE PIC X(10) */
  tranSource: string;
  /** TRAN-DESC PIC X(100) */
  tranDesc: string;
  /** TRAN-AMT PIC S9(09)V99 */
  tranAmt: Decimal;
  /** TRAN-MERCHANT-ID PIC 9(09) */
  tranMerchantId: string;
  /** TRAN-MERCHANT-NAME PIC X(50) */
  tranMerchantName: string;
  /** TRAN-MERCHANT-CITY PIC X(50) */
  tranMerchantCity: string;
  /** TRAN-MERCHANT-ZIP PIC X(10) */
  tranMerchantZip: string;
  /** TRAN-CARD-NUM PIC X(16) */
  tranCardNum: string;
  /** TRAN-ORIG-TS PIC X(26) */
  tranOrigTs: string;
  /** TRAN-PROC-TS PIC X(26) */
  tranProcTs: string;
}

export const TRAN_LAYOUT = defineLayout<TranRecord>('TRAN-RECORD (CVTRA05Y)', [
  { kind: 'alpha', name: 'tranId', length: 16 },
  { kind: 'alpha', name: 'tranTypeCd', length: 2 },
  { kind: 'unsigned', name: 'tranCatCd', length: 4 },
  { kind: 'alpha', name: 'tranSource', length: 10 },
  { kind: 'alpha', name: 'tranDesc', length: 100 },
  { kind: 'signed', name: 'tranAmt', intDigits: 9, scale: 2 },
  { kind: 'unsigned', name: 'tranMerchantId', length: 9 },
  { kind: 'alpha', name: 'tranMerchantName', length: 50 },
  { kind: 'alpha', name: 'tranMerchantCity', length: 50 },
  { kind: 'alpha', name: 'tranMerchantZip', length: 10 },
  { kind: 'alpha', name: 'tranCardNum', length: 16 },
  { kind: 'alpha', name: 'tranOrigTs', length: 26 },
  { kind: 'alpha', name: 'tranProcTs', length: 26 },
  { kind: 'filler', length: 20 },
]);

export const tranKey = (r: TranRecord): string => r.tranId.padEnd(16, ' ');
