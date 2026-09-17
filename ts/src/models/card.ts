import { RecordLayout } from './layout';

/**
 * CARD-RECORD (copybook CVACT02Y, LRECL 150, dataset CARDDATA).
 *
 * off  len  pic        field
 *   0   16  X(16)      CARD-NUM
 *  16   11  9(11)      CARD-ACCT-ID
 *  27    3  9(03)      CARD-CVV-CD
 *  30   50  X(50)      CARD-EMBOSSED-NAME
 *  80   10  X(10)      CARD-EXPIRAION-DATE
 *  90    1  X(01)      CARD-ACTIVE-STATUS
 *  91   59  X(59)      FILLER
 */
export interface CardRecord {
  cardNum: string;
  cardAcctId: number;
  cardCvvCd: number;
  cardEmbossedName: string;
  cardExpiraionDate: string;
  cardActiveStatus: string;
}

export const CARD_RECORD_LENGTH = 150;

export const CARD_LAYOUT: RecordLayout<CardRecord> = {
  copybook: 'CVACT02Y',
  length: CARD_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'cardNum', length: 16 },
    { kind: 'unsigned', name: 'cardAcctId', length: 11 },
    { kind: 'unsigned', name: 'cardCvvCd', length: 3 },
    { kind: 'alphanumeric', name: 'cardEmbossedName', length: 50 },
    { kind: 'alphanumeric', name: 'cardExpiraionDate', length: 10 },
    { kind: 'alphanumeric', name: 'cardActiveStatus', length: 1 },
    { kind: 'filler', length: 59 },
  ],
  fromFields: (v) => ({
    cardNum: v.cardNum as string,
    cardAcctId: v.cardAcctId as number,
    cardCvvCd: v.cardCvvCd as number,
    cardEmbossedName: v.cardEmbossedName as string,
    cardExpiraionDate: v.cardExpiraionDate as string,
    cardActiveStatus: v.cardActiveStatus as string,
  }),
  toFields: (r) => ({ ...r }),
};
