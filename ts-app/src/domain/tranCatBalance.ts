import type { Decimal } from 'decimal.js';
import { alnum, digits, filler, signed, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CVTRA01Y.cpy` — TRAN-CAT-BAL-RECORD (RECLN 50). */
export interface TranCatBalanceRecord {
  /** TRANCAT-ACCT-ID PIC 9(11) */
  trancatAcctId: string;
  /** TRANCAT-TYPE-CD PIC X(02) */
  trancatTypeCd: string;
  /** TRANCAT-CD PIC 9(04) */
  trancatCd: string;
  /** TRAN-CAT-BAL PIC S9(09)V99 */
  tranCatBal: Decimal;
}

export const TRAN_CAT_BALANCE_LAYOUT: RecordLayout<TranCatBalanceRecord> = {
  copybook: 'CVTRA01Y',
  recordLength: 50,
  fields: [
    digits('trancatAcctId', 11),
    alnum('trancatTypeCd', 2),
    digits('trancatCd', 4),
    signed('tranCatBal', 11, 2),
    filler(22),
  ],
};

/** TRAN-CAT-KEY — the KSDS key of the category balance file. */
export function tranCatKey(acctId: string, typeCd: string, catCd: string): string {
  return `${acctId.padStart(11, '0')}${typeCd.padEnd(2, ' ')}${catCd.padStart(4, '0')}`;
}
