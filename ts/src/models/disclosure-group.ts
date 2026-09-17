/**
 * Generated from app/cpy/CVTRA02Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 50 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface DisclosureGroupRecord {
  /** DIS-ACCT-GROUP-ID PIC X(10), in DIS-GROUP-KEY, bytes 1-10 */
  disAcctGroupId: string;
  /** DIS-TRAN-TYPE-CD PIC X(02), in DIS-GROUP-KEY, bytes 11-12 */
  disTranTypeCd: string;
  /** DIS-TRAN-CAT-CD PIC 9(04), in DIS-GROUP-KEY, bytes 13-16 */
  disTranCatCd: number;
  /** DIS-INT-RATE PIC S9(04)V99, bytes 17-22 */
  disIntRate: number;
}

export const DISCLOSURE_GROUP_RECORD_LENGTH = 50;

export const DISCLOSURE_GROUP_RECORD_SPEC: RecordSpec<DisclosureGroupRecord> = {
  name: 'DisclosureGroupRecord',
  copybook: 'app/cpy/CVTRA02Y.cpy',
  recordLength: 50,
  fields: [
    {
      name: 'disAcctGroupId',
      cobolName: 'DIS-ACCT-GROUP-ID',
      offset: 0,
      length: 10,
      filler: false,
      picture: {
        pic: 'X(10)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 10,
      },
    },
    {
      name: 'disTranTypeCd',
      cobolName: 'DIS-TRAN-TYPE-CD',
      offset: 10,
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
      name: 'disTranCatCd',
      cobolName: 'DIS-TRAN-CAT-CD',
      offset: 12,
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
      name: 'disIntRate',
      cobolName: 'DIS-INT-RATE',
      offset: 16,
      length: 6,
      filler: false,
      picture: {
        pic: 'S9(04)V99',
        kind: 'numeric',
        digits: 6,
        scale: 2,
        signed: true,
        packed: false,
        length: 6,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 22,
      length: 28,
      filler: true,
      picture: {
        pic: 'X(28)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 28,
      },
    },
  ],
};
