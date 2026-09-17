import { RecordLayout } from './layout';

/**
 * TRNX-RECORD (copybook COSTM01, LRECL 350) — the transaction view used by the
 * statement job CBSTM03A/CBSTM03B, keyed on card number + transaction id.
 *
 * off  len  pic          field
 *   0   16  X(16)        TRNX-CARD-NUM \
 *  16   16  X(16)        TRNX-ID        > TRNX-KEY
 *  32    2  X(02)        TRNX-TYPE-CD
 *  34    4  9(04)        TRNX-CAT-CD
 *  38   10  X(10)        TRNX-SOURCE
 *  48  100  X(100)       TRNX-DESC
 * 148   11  S9(09)V99    TRNX-AMT
 * 159    9  9(09)        TRNX-MERCHANT-ID
 * 168   50  X(50)        TRNX-MERCHANT-NAME
 * 218   50  X(50)        TRNX-MERCHANT-CITY
 * 268   10  X(10)        TRNX-MERCHANT-ZIP
 * 278   26  X(26)        TRNX-ORIG-TS
 * 304   26  X(26)        TRNX-PROC-TS
 * 330   20  X(20)        FILLER
 */
export interface StatementTransactionRecord {
  trnxCardNum: string;
  trnxId: string;
  trnxTypeCd: string;
  trnxCatCd: number;
  trnxSource: string;
  trnxDesc: string;
  trnxAmt: number;
  trnxMerchantId: number;
  trnxMerchantName: string;
  trnxMerchantCity: string;
  trnxMerchantZip: string;
  trnxOrigTs: string;
  trnxProcTs: string;
}

export const STATEMENT_TRANSACTION_RECORD_LENGTH = 350;

export const STATEMENT_TRANSACTION_LAYOUT: RecordLayout<StatementTransactionRecord> = {
  copybook: 'COSTM01',
  length: STATEMENT_TRANSACTION_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'trnxCardNum', length: 16 },
    { kind: 'alphanumeric', name: 'trnxId', length: 16 },
    { kind: 'alphanumeric', name: 'trnxTypeCd', length: 2 },
    { kind: 'unsigned', name: 'trnxCatCd', length: 4 },
    { kind: 'alphanumeric', name: 'trnxSource', length: 10 },
    { kind: 'alphanumeric', name: 'trnxDesc', length: 100 },
    { kind: 'signed', name: 'trnxAmt', length: 11, scale: 2 },
    { kind: 'unsigned', name: 'trnxMerchantId', length: 9 },
    { kind: 'alphanumeric', name: 'trnxMerchantName', length: 50 },
    { kind: 'alphanumeric', name: 'trnxMerchantCity', length: 50 },
    { kind: 'alphanumeric', name: 'trnxMerchantZip', length: 10 },
    { kind: 'alphanumeric', name: 'trnxOrigTs', length: 26 },
    { kind: 'alphanumeric', name: 'trnxProcTs', length: 26 },
    { kind: 'filler', length: 20 },
  ],
  fromFields: (v) => ({
    trnxCardNum: v.trnxCardNum as string,
    trnxId: v.trnxId as string,
    trnxTypeCd: v.trnxTypeCd as string,
    trnxCatCd: v.trnxCatCd as number,
    trnxSource: v.trnxSource as string,
    trnxDesc: v.trnxDesc as string,
    trnxAmt: v.trnxAmt as number,
    trnxMerchantId: v.trnxMerchantId as number,
    trnxMerchantName: v.trnxMerchantName as string,
    trnxMerchantCity: v.trnxMerchantCity as string,
    trnxMerchantZip: v.trnxMerchantZip as string,
    trnxOrigTs: v.trnxOrigTs as string,
    trnxProcTs: v.trnxProcTs as string,
  }),
  toFields: (r) => ({ ...r }),
};

export function statementTransactionKey(record: { trnxCardNum: string; trnxId: string }): string {
  return record.trnxCardNum.padEnd(16, ' ') + record.trnxId.padEnd(16, ' ');
}
