import { CARD_LAYOUT, CardRecord } from '../models/card';
import { KsdsFile } from './ksds';

/**
 * CARDDATA (MFE.CARDDEMO.CARDDATA, CICS file CARDDAT) keyed on CARD-NUM(16),
 * with alternate index CARDAIX on CARD-ACCT-ID(11).
 */
export function cardKey(cardNum: string | number): string {
  return String(cardNum).trim().padEnd(16, ' ');
}

export const CARDAIX = 'CARDAIX';

export class CardRepository {
  readonly file = new KsdsFile<CardRecord>({
    name: 'CARDDAT',
    layout: CARD_LAYOUT,
    keyOf: (record) => cardKey(record.cardNum),
    alternateKeys: {
      [CARDAIX]: (record) => String(record.cardAcctId).padStart(11, '0'),
    },
  });

  load(records: Iterable<CardRecord>): void {
    this.file.load(records);
  }

  read(cardNum: string | number): CardRecord {
    return this.file.read(cardKey(cardNum));
  }

  tryRead(cardNum: string | number): CardRecord | undefined {
    return this.file.tryRead(cardKey(cardNum));
  }

  readAll(): CardRecord[] {
    return this.file.readAll();
  }

  /** Browse through the CARDAIX path for every card of an account. */
  readByAccountId(acctId: number | string): CardRecord[] {
    return this.file.readByAlternate(CARDAIX, String(acctId).trim().padStart(11, '0'));
  }

  write(record: CardRecord): void {
    this.file.write(record);
  }

  rewrite(record: CardRecord): void {
    this.file.rewrite(record);
  }

  delete(cardNum: string | number): void {
    this.file.delete(cardKey(cardNum));
  }

  startBrowse(cardNum?: string | number) {
    return this.file.startBrowse(cardNum === undefined ? undefined : cardKey(cardNum));
  }
}
