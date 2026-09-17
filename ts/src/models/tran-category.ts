/**
 * Generated from app/cpy/CVTRA04Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 60 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface TranCategoryRecord {
  /** TRAN-TYPE-CD PIC X(02), in TRAN-CAT-KEY, bytes 1-2 */
  tranTypeCd: string;
  /** TRAN-CAT-CD PIC 9(04), in TRAN-CAT-KEY, bytes 3-6 */
  tranCatCd: number;
  /** TRAN-CAT-TYPE-DESC PIC X(50), bytes 7-56 */
  tranCatTypeDesc: string;
}

export const TRAN_CATEGORY_RECORD_LENGTH = 60;

export const TRAN_CATEGORY_RECORD_SPEC: RecordSpec<TranCategoryRecord> = {
  name: 'TranCategoryRecord',
  copybook: 'app/cpy/CVTRA04Y.cpy',
  recordLength: 60,
  fields: [
    {
      name: 'tranTypeCd',
      cobolName: 'TRAN-TYPE-CD',
      offset: 0,
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
      name: 'tranCatCd',
      cobolName: 'TRAN-CAT-CD',
      offset: 2,
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
      name: 'tranCatTypeDesc',
      cobolName: 'TRAN-CAT-TYPE-DESC',
      offset: 6,
      length: 50,
      filler: false,
      picture: {
        pic: 'X(50)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 50,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 56,
      length: 4,
      filler: true,
      picture: {
        pic: 'X(04)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 4,
      },
    },
  ],
};
