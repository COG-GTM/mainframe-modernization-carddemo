import type { Decimal } from 'decimal.js';
import { alnum, digits, filler, signed, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CVTRA05Y.cpy` — TRAN-RECORD (RECLN 350). */
export interface TransactionRecord {
  /** TRAN-ID PIC X(16) */
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

export const TRANSACTION_LAYOUT: RecordLayout<TransactionRecord> = {
  copybook: 'CVTRA05Y',
  recordLength: 350,
  fields: [
    alnum('tranId', 16),
    alnum('tranTypeCd', 2),
    digits('tranCatCd', 4),
    alnum('tranSource', 10),
    alnum('tranDesc', 100),
    signed('tranAmt', 11, 2),
    digits('tranMerchantId', 9),
    alnum('tranMerchantName', 50),
    alnum('tranMerchantCity', 50),
    alnum('tranMerchantZip', 10),
    alnum('tranCardNum', 16),
    alnum('tranOrigTs', 26),
    alnum('tranProcTs', 26),
    filler(20),
  ],
};
