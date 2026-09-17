/**
 * Generated from app/cpy/CVACT02Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 150 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface CardRecord {
  /** CARD-NUM PIC X(16), bytes 1-16 */
  cardNum: string;
  /** CARD-ACCT-ID PIC 9(11), bytes 17-27 */
  cardAcctId: number;
  /** CARD-CVV-CD PIC 9(03), bytes 28-30 */
  cardCvvCd: number;
  /** CARD-EMBOSSED-NAME PIC X(50), bytes 31-80 */
  cardEmbossedName: string;
  /** CARD-EXPIRAION-DATE PIC X(10), bytes 81-90 */
  cardExpiraionDate: string;
  /** CARD-ACTIVE-STATUS PIC X(01), bytes 91-91 */
  cardActiveStatus: string;
}

export const CARD_RECORD_LENGTH = 150;

export const CARD_RECORD_SPEC: RecordSpec<CardRecord> = {
  name: 'CardRecord',
  copybook: 'app/cpy/CVACT02Y.cpy',
  recordLength: 150,
  fields: [
    {
      name: 'cardNum',
      cobolName: 'CARD-NUM',
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
      name: 'cardAcctId',
      cobolName: 'CARD-ACCT-ID',
      offset: 16,
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
      name: 'cardCvvCd',
      cobolName: 'CARD-CVV-CD',
      offset: 27,
      length: 3,
      filler: false,
      picture: {
        pic: '9(03)',
        kind: 'numeric',
        digits: 3,
        scale: 0,
        signed: false,
        packed: false,
        length: 3,
      },
    },
    {
      name: 'cardEmbossedName',
      cobolName: 'CARD-EMBOSSED-NAME',
      offset: 30,
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
      name: 'cardExpiraionDate',
      cobolName: 'CARD-EXPIRAION-DATE',
      offset: 80,
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
      name: 'cardActiveStatus',
      cobolName: 'CARD-ACTIVE-STATUS',
      offset: 90,
      length: 1,
      filler: false,
      picture: {
        pic: 'X(01)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 1,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 91,
      length: 59,
      filler: true,
      picture: {
        pic: 'X(59)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 59,
      },
    },
  ],
};
