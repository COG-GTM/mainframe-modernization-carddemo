/**
 * Generated from app/cpy/CVTRA06Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 350 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface DailyTransactionRecord {
  /** DALYTRAN-ID PIC X(16), bytes 1-16 */
  dalytranId: string;
  /** DALYTRAN-TYPE-CD PIC X(02), bytes 17-18 */
  dalytranTypeCd: string;
  /** DALYTRAN-CAT-CD PIC 9(04), bytes 19-22 */
  dalytranCatCd: number;
  /** DALYTRAN-SOURCE PIC X(10), bytes 23-32 */
  dalytranSource: string;
  /** DALYTRAN-DESC PIC X(100), bytes 33-132 */
  dalytranDesc: string;
  /** DALYTRAN-AMT PIC S9(09)V99, bytes 133-143 */
  dalytranAmt: number;
  /** DALYTRAN-MERCHANT-ID PIC 9(09), bytes 144-152 */
  dalytranMerchantId: number;
  /** DALYTRAN-MERCHANT-NAME PIC X(50), bytes 153-202 */
  dalytranMerchantName: string;
  /** DALYTRAN-MERCHANT-CITY PIC X(50), bytes 203-252 */
  dalytranMerchantCity: string;
  /** DALYTRAN-MERCHANT-ZIP PIC X(10), bytes 253-262 */
  dalytranMerchantZip: string;
  /** DALYTRAN-CARD-NUM PIC X(16), bytes 263-278 */
  dalytranCardNum: string;
  /** DALYTRAN-ORIG-TS PIC X(26), bytes 279-304 */
  dalytranOrigTs: string;
  /** DALYTRAN-PROC-TS PIC X(26), bytes 305-330 */
  dalytranProcTs: string;
}

export const DAILY_TRANSACTION_RECORD_LENGTH = 350;

export const DAILY_TRANSACTION_RECORD_SPEC: RecordSpec<DailyTransactionRecord> = {
  name: 'DailyTransactionRecord',
  copybook: 'app/cpy/CVTRA06Y.cpy',
  recordLength: 350,
  fields: [
    {
      name: 'dalytranId',
      cobolName: 'DALYTRAN-ID',
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
      name: 'dalytranTypeCd',
      cobolName: 'DALYTRAN-TYPE-CD',
      offset: 16,
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
      name: 'dalytranCatCd',
      cobolName: 'DALYTRAN-CAT-CD',
      offset: 18,
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
      name: 'dalytranSource',
      cobolName: 'DALYTRAN-SOURCE',
      offset: 22,
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
      name: 'dalytranDesc',
      cobolName: 'DALYTRAN-DESC',
      offset: 32,
      length: 100,
      filler: false,
      picture: {
        pic: 'X(100)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 100,
      },
    },
    {
      name: 'dalytranAmt',
      cobolName: 'DALYTRAN-AMT',
      offset: 132,
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
      name: 'dalytranMerchantId',
      cobolName: 'DALYTRAN-MERCHANT-ID',
      offset: 143,
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
      name: 'dalytranMerchantName',
      cobolName: 'DALYTRAN-MERCHANT-NAME',
      offset: 152,
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
      name: 'dalytranMerchantCity',
      cobolName: 'DALYTRAN-MERCHANT-CITY',
      offset: 202,
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
      name: 'dalytranMerchantZip',
      cobolName: 'DALYTRAN-MERCHANT-ZIP',
      offset: 252,
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
      name: 'dalytranCardNum',
      cobolName: 'DALYTRAN-CARD-NUM',
      offset: 262,
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
      name: 'dalytranOrigTs',
      cobolName: 'DALYTRAN-ORIG-TS',
      offset: 278,
      length: 26,
      filler: false,
      picture: {
        pic: 'X(26)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 26,
      },
    },
    {
      name: 'dalytranProcTs',
      cobolName: 'DALYTRAN-PROC-TS',
      offset: 304,
      length: 26,
      filler: false,
      picture: {
        pic: 'X(26)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 26,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 330,
      length: 20,
      filler: true,
      picture: {
        pic: 'X(20)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 20,
      },
    },
  ],
};
