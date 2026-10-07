import { defineLayout } from './layout.js';

/**
 * CVACT02Y – CARD-RECORD (CARDDATA KSDS, RECLN 150, key CARD-NUM).
 * Not read by CBTRN02C itself; ported so the data layer can serve card
 * lookups to related batch programs.
 */
export interface CardRecord {
  /** CARD-NUM PIC X(16) – record key */
  cardNum: string;
  /** CARD-ACCT-ID PIC 9(11) */
  cardAcctId: string;
  /** CARD-CVV-CD PIC 9(03) */
  cardCvvCd: string;
  /** CARD-EMBOSSED-NAME PIC X(50) */
  cardEmbossedName: string;
  /** CARD-EXPIRAION-DATE PIC X(10) */
  cardExpirationDate: string;
  /** CARD-ACTIVE-STATUS PIC X(01) */
  cardActiveStatus: string;
}

export const CARD_LAYOUT = defineLayout<CardRecord>('CARD-RECORD (CVACT02Y)', [
  { kind: 'alpha', name: 'cardNum', length: 16 },
  { kind: 'unsigned', name: 'cardAcctId', length: 11 },
  { kind: 'unsigned', name: 'cardCvvCd', length: 3 },
  { kind: 'alpha', name: 'cardEmbossedName', length: 50 },
  { kind: 'alpha', name: 'cardExpirationDate', length: 10 },
  { kind: 'alpha', name: 'cardActiveStatus', length: 1 },
  { kind: 'filler', length: 59 },
]);

export const cardKey = (r: CardRecord): string => r.cardNum.padEnd(16, ' ');
