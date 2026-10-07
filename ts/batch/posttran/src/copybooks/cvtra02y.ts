import type { Decimal } from 'decimal.js';
import { defineLayout } from './layout.js';

/**
 * CVTRA02Y – DIS-GROUP-RECORD (DISCGRP KSDS, RECLN 50, key DIS-GROUP-KEY).
 * Disclosure-group interest rates. Not read by CBTRN02C (used by CBACT04C /
 * INTCALC); ported so the data layer is complete for follow-on programs.
 */
export interface DisGroupRecord {
  /** DIS-GROUP-KEY.DIS-ACCT-GROUP-ID PIC X(10) */
  disAcctGroupId: string;
  /** DIS-GROUP-KEY.DIS-TRAN-TYPE-CD PIC X(02) */
  disTranTypeCd: string;
  /** DIS-GROUP-KEY.DIS-TRAN-CAT-CD PIC 9(04) */
  disTranCatCd: string;
  /** DIS-INT-RATE PIC S9(04)V99 */
  disIntRate: Decimal;
}

export const DIS_GROUP_LAYOUT = defineLayout<DisGroupRecord>('DIS-GROUP-RECORD (CVTRA02Y)', [
  { kind: 'alpha', name: 'disAcctGroupId', length: 10 },
  { kind: 'alpha', name: 'disTranTypeCd', length: 2 },
  { kind: 'unsigned', name: 'disTranCatCd', length: 4 },
  { kind: 'signed', name: 'disIntRate', intDigits: 4, scale: 2 },
  { kind: 'filler', length: 28 },
]);

export const disGroupKey = (r: DisGroupRecord): string =>
  r.disAcctGroupId.padEnd(10, ' ') + r.disTranTypeCd.padEnd(2, ' ') + r.disTranCatCd;
