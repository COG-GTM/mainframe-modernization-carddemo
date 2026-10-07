import type { Decimal } from 'decimal.js';
import { defineLayout } from './layout.js';

/** CVTRA06Y – DALYTRAN-RECORD (daily transaction input, RECLN 350). */
export interface DailyTranRecord {
  /** DALYTRAN-ID PIC X(16) */
  dalytranId: string;
  /** DALYTRAN-TYPE-CD PIC X(02) */
  dalytranTypeCd: string;
  /** DALYTRAN-CAT-CD PIC 9(04) */
  dalytranCatCd: string;
  /** DALYTRAN-SOURCE PIC X(10) */
  dalytranSource: string;
  /** DALYTRAN-DESC PIC X(100) */
  dalytranDesc: string;
  /** DALYTRAN-AMT PIC S9(09)V99 */
  dalytranAmt: Decimal;
  /** DALYTRAN-MERCHANT-ID PIC 9(09) */
  dalytranMerchantId: string;
  /** DALYTRAN-MERCHANT-NAME PIC X(50) */
  dalytranMerchantName: string;
  /** DALYTRAN-MERCHANT-CITY PIC X(50) */
  dalytranMerchantCity: string;
  /** DALYTRAN-MERCHANT-ZIP PIC X(10) */
  dalytranMerchantZip: string;
  /** DALYTRAN-CARD-NUM PIC X(16) */
  dalytranCardNum: string;
  /** DALYTRAN-ORIG-TS PIC X(26) */
  dalytranOrigTs: string;
  /** DALYTRAN-PROC-TS PIC X(26) */
  dalytranProcTs: string;
}

export const DAILY_TRAN_LAYOUT = defineLayout<DailyTranRecord>('DALYTRAN-RECORD (CVTRA06Y)', [
  { kind: 'alpha', name: 'dalytranId', length: 16 },
  { kind: 'alpha', name: 'dalytranTypeCd', length: 2 },
  { kind: 'unsigned', name: 'dalytranCatCd', length: 4 },
  { kind: 'alpha', name: 'dalytranSource', length: 10 },
  { kind: 'alpha', name: 'dalytranDesc', length: 100 },
  { kind: 'signed', name: 'dalytranAmt', intDigits: 9, scale: 2 },
  { kind: 'unsigned', name: 'dalytranMerchantId', length: 9 },
  { kind: 'alpha', name: 'dalytranMerchantName', length: 50 },
  { kind: 'alpha', name: 'dalytranMerchantCity', length: 50 },
  { kind: 'alpha', name: 'dalytranMerchantZip', length: 10 },
  { kind: 'alpha', name: 'dalytranCardNum', length: 16 },
  { kind: 'alpha', name: 'dalytranOrigTs', length: 26 },
  { kind: 'alpha', name: 'dalytranProcTs', length: 26 },
  { kind: 'filler', length: 20 },
]);
