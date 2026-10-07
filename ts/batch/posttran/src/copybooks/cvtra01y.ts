import type { Decimal } from 'decimal.js';
import { defineLayout } from './layout.js';

/** CVTRA01Y – TRAN-CAT-BAL-RECORD (TCATBALF KSDS, RECLN 50, key TRAN-CAT-KEY). */
export interface TranCatBalRecord {
  /** TRAN-CAT-KEY.TRANCAT-ACCT-ID PIC 9(11) */
  trancatAcctId: string;
  /** TRAN-CAT-KEY.TRANCAT-TYPE-CD PIC X(02) */
  trancatTypeCd: string;
  /** TRAN-CAT-KEY.TRANCAT-CD PIC 9(04) */
  trancatCd: string;
  /** TRAN-CAT-BAL PIC S9(09)V99 */
  tranCatBal: Decimal;
}

export const TRAN_CAT_BAL_LAYOUT = defineLayout<TranCatBalRecord>('TRAN-CAT-BAL-RECORD (CVTRA01Y)', [
  { kind: 'unsigned', name: 'trancatAcctId', length: 11 },
  { kind: 'alpha', name: 'trancatTypeCd', length: 2 },
  { kind: 'unsigned', name: 'trancatCd', length: 4 },
  { kind: 'signed', name: 'tranCatBal', intDigits: 9, scale: 2 },
  { kind: 'filler', length: 22 },
]);

/** TRAN-CAT-KEY: ACCT-ID(11) + TYPE-CD(2) + CAT-CD(4). */
export function tranCatKeyOf(acctId: string, typeCd: string, catCd: string): string {
  return acctId.padStart(11, '0') + typeCd.padEnd(2, ' ') + catCd.padStart(4, '0');
}

export const tranCatBalKey = (r: TranCatBalRecord): string =>
  tranCatKeyOf(r.trancatAcctId, r.trancatTypeCd, r.trancatCd);
