import { alnum, digits, filler, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CVACT03Y.cpy` — CARD-XREF-RECORD (RECLN 50). */
export interface CardXrefRecord {
  /** XREF-CARD-NUM PIC X(16) */
  xrefCardNum: string;
  /** XREF-CUST-ID PIC 9(09) */
  xrefCustId: string;
  /** XREF-ACCT-ID PIC 9(11) */
  xrefAcctId: string;
}

export const CARD_XREF_LAYOUT: RecordLayout<CardXrefRecord> = {
  copybook: 'CVACT03Y',
  recordLength: 50,
  fields: [alnum('xrefCardNum', 16), digits('xrefCustId', 9), digits('xrefAcctId', 11), filler(14)],
};
