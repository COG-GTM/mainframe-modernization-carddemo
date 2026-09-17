import { RecordLayout } from './layout';

/**
 * CARD-XREF-RECORD (copybook CVACT03Y, LRECL 50, dataset CARDXREF).
 *
 * off  len  pic        field
 *   0   16  X(16)      XREF-CARD-NUM
 *  16    9  9(09)      XREF-CUST-ID
 *  25   11  9(11)      XREF-ACCT-ID
 *  36   14  X(14)      FILLER
 */
export interface CardXrefRecord {
  xrefCardNum: string;
  xrefCustId: number;
  xrefAcctId: number;
}

export const CARD_XREF_RECORD_LENGTH = 50;

export const CARD_XREF_LAYOUT: RecordLayout<CardXrefRecord> = {
  copybook: 'CVACT03Y',
  length: CARD_XREF_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'xrefCardNum', length: 16 },
    { kind: 'unsigned', name: 'xrefCustId', length: 9 },
    { kind: 'unsigned', name: 'xrefAcctId', length: 11 },
    { kind: 'filler', length: 14 },
  ],
  fromFields: (v) => ({
    xrefCardNum: v.xrefCardNum as string,
    xrefCustId: v.xrefCustId as number,
    xrefAcctId: v.xrefAcctId as number,
  }),
  toFields: (r) => ({ ...r }),
};
