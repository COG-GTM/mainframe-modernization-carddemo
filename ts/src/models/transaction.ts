/**
 * Generated from app/cpy/CVTRA05Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 350 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface TransactionRecord {
  /** TRAN-ID PIC X(16), bytes 1-16 */
  tranId: string;
  /** TRAN-TYPE-CD PIC X(02), bytes 17-18 */
  tranTypeCd: string;
  /** TRAN-CAT-CD PIC 9(04), bytes 19-22 */
  tranCatCd: number;
  /** TRAN-SOURCE PIC X(10), bytes 23-32 */
  tranSource: string;
  /** TRAN-DESC PIC X(100), bytes 33-132 */
  tranDesc: string;
  /** TRAN-AMT PIC S9(09)V99, bytes 133-143 */
  tranAmt: number;
  /** TRAN-MERCHANT-ID PIC 9(09), bytes 144-152 */
  tranMerchantId: number;
  /** TRAN-MERCHANT-NAME PIC X(50), bytes 153-202 */
  tranMerchantName: string;
  /** TRAN-MERCHANT-CITY PIC X(50), bytes 203-252 */
  tranMerchantCity: string;
  /** TRAN-MERCHANT-ZIP PIC X(10), bytes 253-262 */
  tranMerchantZip: string;
  /** TRAN-CARD-NUM PIC X(16), bytes 263-278 */
  tranCardNum: string;
  /** TRAN-ORIG-TS PIC X(26), bytes 279-304 */
  tranOrigTs: string;
  /** TRAN-PROC-TS PIC X(26), bytes 305-330 */
  tranProcTs: string;
}

export const TRANSACTION_RECORD_LENGTH = 350;

export const TRANSACTION_RECORD_SPEC: RecordSpec<TransactionRecord> = {
  name: 'TransactionRecord',
  copybook: 'app/cpy/CVTRA05Y.cpy',
  recordLength: 350,
  fields: [
    {
      name: 'tranId',
      cobolName: 'TRAN-ID',
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
      name: 'tranTypeCd',
      cobolName: 'TRAN-TYPE-CD',
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
      name: 'tranCatCd',
      cobolName: 'TRAN-CAT-CD',
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
      name: 'tranSource',
      cobolName: 'TRAN-SOURCE',
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
      name: 'tranDesc',
      cobolName: 'TRAN-DESC',
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
      name: 'tranAmt',
      cobolName: 'TRAN-AMT',
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
      name: 'tranMerchantId',
      cobolName: 'TRAN-MERCHANT-ID',
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
      name: 'tranMerchantName',
      cobolName: 'TRAN-MERCHANT-NAME',
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
      name: 'tranMerchantCity',
      cobolName: 'TRAN-MERCHANT-CITY',
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
      name: 'tranMerchantZip',
      cobolName: 'TRAN-MERCHANT-ZIP',
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
      name: 'tranCardNum',
      cobolName: 'TRAN-CARD-NUM',
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
      name: 'tranOrigTs',
      cobolName: 'TRAN-ORIG-TS',
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
      name: 'tranProcTs',
      cobolName: 'TRAN-PROC-TS',
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
