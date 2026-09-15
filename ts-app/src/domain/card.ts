import { alnum, digits, filler, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CVACT02Y.cpy` — CARD-RECORD (RECLN 150). */
export interface CardRecord {
  /** CARD-NUM PIC X(16) */
  cardNum: string;
  /** CARD-ACCT-ID PIC 9(11) */
  cardAcctId: string;
  /** CARD-CVV-CD PIC 9(03) */
  cardCvvCd: string;
  /** CARD-EMBOSSED-NAME PIC X(50) */
  cardEmbossedName: string;
  /** CARD-EXPIRAION-DATE PIC X(10) (COBOL spelling preserved) */
  cardExpiraionDate: string;
  /** CARD-ACTIVE-STATUS PIC X(01) */
  cardActiveStatus: string;
}

export const CARD_LAYOUT: RecordLayout<CardRecord> = {
  copybook: 'CVACT02Y',
  recordLength: 150,
  fields: [
    alnum('cardNum', 16),
    digits('cardAcctId', 11),
    digits('cardCvvCd', 3),
    alnum('cardEmbossedName', 50),
    alnum('cardExpiraionDate', 10),
    alnum('cardActiveStatus', 1),
    filler(59),
  ],
};
