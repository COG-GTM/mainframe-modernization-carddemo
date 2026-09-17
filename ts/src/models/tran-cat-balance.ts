/**
 * Generated from app/cpy/CVTRA01Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 50 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface TranCatBalanceRecord {
  /** TRANCAT-ACCT-ID PIC 9(11), in TRAN-CAT-KEY, bytes 1-11 */
  trancatAcctId: number;
  /** TRANCAT-TYPE-CD PIC X(02), in TRAN-CAT-KEY, bytes 12-13 */
  trancatTypeCd: string;
  /** TRANCAT-CD PIC 9(04), in TRAN-CAT-KEY, bytes 14-17 */
  trancatCd: number;
  /** TRAN-CAT-BAL PIC S9(09)V99, bytes 18-28 */
  tranCatBal: number;
}

export const TRAN_CAT_BALANCE_RECORD_LENGTH = 50;

export const TRAN_CAT_BALANCE_RECORD_SPEC: RecordSpec<TranCatBalanceRecord> = {
  name: 'TranCatBalanceRecord',
  copybook: 'app/cpy/CVTRA01Y.cpy',
  recordLength: 50,
  fields: [
    {
      name: 'trancatAcctId',
      cobolName: 'TRANCAT-ACCT-ID',
      offset: 0,
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
      name: 'trancatTypeCd',
      cobolName: 'TRANCAT-TYPE-CD',
      offset: 11,
      length: 2,
      filler: false,
      picture: {
        pic: 'X(02)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 2,
      },
    },
    {
      name: 'trancatCd',
      cobolName: 'TRANCAT-CD',
      offset: 13,
      length: 4,
      filler: false,
      picture: {
        pic: '9(04)',
        kind: 'numeric',
        digits: 4,
        scale: 0,
        signed: false,
        packed: false,
        length: 4,
      },
    },
    {
      name: 'tranCatBal',
      cobolName: 'TRAN-CAT-BAL',
      offset: 17,
      length: 11,
      filler: false,
      picture: {
        pic: 'S9(09)V99',
        kind: 'numeric',
        digits: 11,
        scale: 2,
        signed: true,
        packed: false,
        length: 11,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 28,
      length: 22,
      filler: true,
      picture: {
        pic: 'X(22)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 22,
      },
    },
  ],
};
