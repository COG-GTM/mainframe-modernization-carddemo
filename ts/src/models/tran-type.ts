/**
 * Generated from app/cpy/CVTRA03Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 60 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface TranTypeRecord {
  /** TRAN-TYPE PIC X(02), bytes 1-2 */
  tranType: string;
  /** TRAN-TYPE-DESC PIC X(50), bytes 3-52 */
  tranTypeDesc: string;
}

export const TRAN_TYPE_RECORD_LENGTH = 60;

export const TRAN_TYPE_RECORD_SPEC: RecordSpec<TranTypeRecord> = {
  name: 'TranTypeRecord',
  copybook: 'app/cpy/CVTRA03Y.cpy',
  recordLength: 60,
  fields: [
    {
      name: 'tranType',
      cobolName: 'TRAN-TYPE',
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
      name: 'tranTypeDesc',
      cobolName: 'TRAN-TYPE-DESC',
      offset: 2,
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
      offset: 52,
      length: 8,
      filler: true,
      picture: {
        pic: 'X(08)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 8,
      },
    },
  ],
};
