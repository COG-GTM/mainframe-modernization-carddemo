import { RecordLayout } from './layout';

/**
 * TRAN-RECORD (copybook CVTRA05Y, LRECL 350, dataset TRANSACT KSDS keyed on
 * TRAN-ID) and DALYTRAN-RECORD (copybook CVTRA06Y, LRECL 350, dataset DALYTRAN)
 * which is byte-for-byte identical with a DALYTRAN- field prefix.
 *
 * off  len  pic          field
 *   0   16  X(16)        TRAN-ID
 *  16    2  X(02)        TRAN-TYPE-CD
 *  18    4  9(04)        TRAN-CAT-CD
 *  22   10  X(10)        TRAN-SOURCE
 *  32  100  X(100)       TRAN-DESC
 * 132   11  S9(09)V99    TRAN-AMT
 * 143    9  9(09)        TRAN-MERCHANT-ID
 * 152   50  X(50)        TRAN-MERCHANT-NAME
 * 202   50  X(50)        TRAN-MERCHANT-CITY
 * 252   10  X(10)        TRAN-MERCHANT-ZIP
 * 262   16  X(16)        TRAN-CARD-NUM
 * 278   26  X(26)        TRAN-ORIG-TS
 * 304   26  X(26)        TRAN-PROC-TS
 * 330   20  X(20)        FILLER
 */
export interface TransactionRecord {
  tranId: string;
  tranTypeCd: string;
  tranCatCd: number;
  tranSource: string;
  tranDesc: string;
  tranAmt: number;
  tranMerchantId: number;
  tranMerchantName: string;
  tranMerchantCity: string;
  tranMerchantZip: string;
  tranCardNum: string;
  tranOrigTs: string;
  tranProcTs: string;
}

export const TRANSACTION_RECORD_LENGTH = 350;

export const TRANSACTION_LAYOUT: RecordLayout<TransactionRecord> = {
  copybook: 'CVTRA05Y',
  length: TRANSACTION_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'tranId', length: 16 },
    { kind: 'alphanumeric', name: 'tranTypeCd', length: 2 },
    { kind: 'unsigned', name: 'tranCatCd', length: 4 },
    { kind: 'alphanumeric', name: 'tranSource', length: 10 },
    { kind: 'alphanumeric', name: 'tranDesc', length: 100 },
    { kind: 'signed', name: 'tranAmt', length: 11, scale: 2 },
    { kind: 'unsigned', name: 'tranMerchantId', length: 9 },
    { kind: 'alphanumeric', name: 'tranMerchantName', length: 50 },
    { kind: 'alphanumeric', name: 'tranMerchantCity', length: 50 },
    { kind: 'alphanumeric', name: 'tranMerchantZip', length: 10 },
    { kind: 'alphanumeric', name: 'tranCardNum', length: 16 },
    { kind: 'alphanumeric', name: 'tranOrigTs', length: 26 },
    { kind: 'alphanumeric', name: 'tranProcTs', length: 26 },
    { kind: 'filler', length: 20 },
  ],
  fromFields: (v) => ({
    tranId: v.tranId as string,
    tranTypeCd: v.tranTypeCd as string,
    tranCatCd: v.tranCatCd as number,
    tranSource: v.tranSource as string,
    tranDesc: v.tranDesc as string,
    tranAmt: v.tranAmt as number,
    tranMerchantId: v.tranMerchantId as number,
    tranMerchantName: v.tranMerchantName as string,
    tranMerchantCity: v.tranMerchantCity as string,
    tranMerchantZip: v.tranMerchantZip as string,
    tranCardNum: v.tranCardNum as string,
    tranOrigTs: v.tranOrigTs as string,
    tranProcTs: v.tranProcTs as string,
  }),
  toFields: (r) => ({ ...r }),
};

/** DALYTRAN-RECORD shares the TRAN-RECORD layout (CVTRA06Y, LRECL 350). */
export type DailyTransactionRecord = TransactionRecord;

export const DAILY_TRANSACTION_RECORD_LENGTH = TRANSACTION_RECORD_LENGTH;

export const DAILY_TRANSACTION_LAYOUT: RecordLayout<DailyTransactionRecord> = {
  ...TRANSACTION_LAYOUT,
  copybook: 'CVTRA06Y',
};
