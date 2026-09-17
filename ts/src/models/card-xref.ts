/**
 * Generated from app/cpy/CVACT03Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 50 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface CardXrefRecord {
  /** XREF-CARD-NUM PIC X(16), bytes 1-16 */
  xrefCardNum: string;
  /** XREF-CUST-ID PIC 9(09), bytes 17-25 */
  xrefCustId: number;
  /** XREF-ACCT-ID PIC 9(11), bytes 26-36 */
  xrefAcctId: number;
}

export const CARD_XREF_RECORD_LENGTH = 50;

export const CARD_XREF_RECORD_SPEC: RecordSpec<CardXrefRecord> = {
  name: 'CardXrefRecord',
  copybook: 'app/cpy/CVACT03Y.cpy',
  recordLength: 50,
  fields: [
    {
      name: 'xrefCardNum',
      cobolName: 'XREF-CARD-NUM',
      offset: 0,
      length: 16,
      filler: false,
      picture: {
        pic: 'X(16)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 16,
      },
    },
    {
      name: 'xrefCustId',
      cobolName: 'XREF-CUST-ID',
      offset: 16,
      length: 9,
      filler: false,
      picture: {
        pic: '9(09)',
        kind: 'numeric',
        digits: 9,
        scale: 0,
        signed: false,
        packed: false,
        length: 9,
      },
    },
    {
      name: 'xrefAcctId',
      cobolName: 'XREF-ACCT-ID',
      offset: 25,
      length: 11,
      filler: false,
      picture: {
        pic: '9(11)',
        kind: 'numeric',
        digits: 11,
        scale: 0,
        signed: false,
        packed: false,
        length: 11,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 36,
      length: 14,
      filler: true,
      picture: {
        pic: 'X(14)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 14,
      },
    },
  ],
};
