import type { Decimal } from 'decimal.js';
import { alnum, digits, filler, signed, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CVTRA06Y.cpy` — DALYTRAN-RECORD (RECLN 350). */
export interface DailyTransactionRecord {
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

export const DAILY_TRANSACTION_LAYOUT: RecordLayout<DailyTransactionRecord> = {
  copybook: 'CVTRA06Y',
  recordLength: 350,
  fields: [
    alnum('dalytranId', 16),
    alnum('dalytranTypeCd', 2),
    digits('dalytranCatCd', 4),
    alnum('dalytranSource', 10),
    alnum('dalytranDesc', 100),
    signed('dalytranAmt', 11, 2),
    digits('dalytranMerchantId', 9),
    alnum('dalytranMerchantName', 50),
    alnum('dalytranMerchantCity', 50),
    alnum('dalytranMerchantZip', 10),
    alnum('dalytranCardNum', 16),
    alnum('dalytranOrigTs', 26),
    alnum('dalytranProcTs', 26),
    filler(20),
  ],
};
