import { defineLayout } from './layout.js';

/** CVACT03Y – CARD-XREF-RECORD (CARDXREF KSDS, RECLN 50, key XREF-CARD-NUM). */
export interface CardXrefRecord {
  /** XREF-CARD-NUM PIC X(16) – record key */
  xrefCardNum: string;
  /** XREF-CUST-ID PIC 9(09) */
  xrefCustId: string;
  /** XREF-ACCT-ID PIC 9(11) */
  xrefAcctId: string;
}

export const CARD_XREF_LAYOUT = defineLayout<CardXrefRecord>('CARD-XREF-RECORD (CVACT03Y)', [
  { kind: 'alpha', name: 'xrefCardNum', length: 16 },
  { kind: 'unsigned', name: 'xrefCustId', length: 9 },
  { kind: 'unsigned', name: 'xrefAcctId', length: 11 },
  { kind: 'filler', length: 14 },
]);

export const cardXrefKey = (r: CardXrefRecord): string => r.xrefCardNum.padEnd(16, ' ');
