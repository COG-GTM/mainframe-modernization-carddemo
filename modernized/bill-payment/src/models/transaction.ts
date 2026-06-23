import { Money } from '../money';

/**
 * Transaction entity — modern equivalent of copybook `CVTRA05Y` (VSAM file
 * TRANSACT, record length 350). Field comments show the original COBOL PIC clause.
 */
export interface Transaction {
  /** TRAN-ID            PIC X(16) — 16-char key; numeric content, zero-padded */
  transactionId: string;
  /** TRAN-TYPE-CD       PIC X(02) */
  typeCode: string;
  /** TRAN-CAT-CD        PIC 9(04) */
  categoryCode: number;
  /** TRAN-SOURCE        PIC X(10) */
  source: string;
  /** TRAN-DESC          PIC X(100) */
  description: string;
  /** TRAN-AMT           PIC S9(09)V99 */
  amount: Money;
  /** TRAN-MERCHANT-ID   PIC 9(09) */
  merchantId: string;
  /** TRAN-MERCHANT-NAME PIC X(50) */
  merchantName: string;
  /** TRAN-MERCHANT-CITY PIC X(50) */
  merchantCity: string;
  /** TRAN-MERCHANT-ZIP  PIC X(10) */
  merchantZip: string;
  /** TRAN-CARD-NUM      PIC X(16) */
  cardNumber: string;
  /** TRAN-ORIG-TS       PIC X(26) — "YYYY-MM-DD HH:MM:SS.mmmmmm" */
  originTimestamp: string;
  /** TRAN-PROC-TS       PIC X(26) */
  processTimestamp: string;
}

/**
 * Constants used by COBIL00C when building the bill-payment transaction record.
 * These mirror the literal MOVEs in the PROCESS-ENTER-KEY paragraph.
 */
export const BILL_PAYMENT_TXN = {
  TYPE_CODE: '02',
  CATEGORY_CODE: 2,
  SOURCE: 'POS TERM',
  DESCRIPTION: 'BILL PAYMENT - ONLINE',
  MERCHANT_ID: '999999999',
  MERCHANT_NAME: 'BILL PAYMENT',
  MERCHANT_CITY: 'N/A',
  MERCHANT_ZIP: 'N/A',
} as const;

export const TRAN_ID_LENGTH = 16;

/** Format a numeric transaction id into the 16-char zero-padded `PIC X(16)` form. */
export function formatTransactionId(value: number): string {
  return value.toString().padStart(TRAN_ID_LENGTH, '0');
}

/**
 * Build a 26-char CICS-style timestamp ("YYYY-MM-DD HH:MM:SS.mmmmmm"),
 * equivalent to GET-CURRENT-TIMESTAMP (ASKTIME + FORMATTIME) in COBIL00C.
 */
export function buildTimestamp(now: Date = new Date()): string {
  const pad = (n: number, w = 2) => n.toString().padStart(w, '0');
  const date = `${now.getUTCFullYear()}-${pad(now.getUTCMonth() + 1)}-${pad(now.getUTCDate())}`;
  const time = `${pad(now.getUTCHours())}:${pad(now.getUTCMinutes())}:${pad(now.getUTCSeconds())}`;
  // COBIL00C sets the microseconds portion (WS-TIMESTAMP-TM-MS6) to zeros.
  return `${date} ${time}.000000`;
}
