import { CARD_XREF_LAYOUT, CardXrefRecord } from '../models/cardXref';
import { KsdsFile } from './ksds';

/**
 * CARDXREF (MFE.CARDDEMO.CARDXREF, CICS file CXACAIX/CCXREF) keyed on
 * XREF-CARD-NUM(16), with alternate index CXACAIX on XREF-ACCT-ID(11).
 */
export function cardXrefKey(cardNum: string | number): string {
  return String(cardNum).trim().padEnd(16, ' ');
}

export const CXACAIX = 'CXACAIX';

export class CardXrefRepository {
  readonly file = new KsdsFile<CardXrefRecord>({
    name: 'CCXREF',
    layout: CARD_XREF_LAYOUT,
    keyOf: (record) => cardXrefKey(record.xrefCardNum),
    alternateKeys: {
      [CXACAIX]: (record) => String(record.xrefAcctId).padStart(11, '0'),
    },
  });

  load(records: Iterable<CardXrefRecord>): void {
    this.file.load(records);
  }

  read(cardNum: string | number): CardXrefRecord {
    return this.file.read(cardXrefKey(cardNum));
  }

  tryRead(cardNum: string | number): CardXrefRecord | undefined {
    return this.file.tryRead(cardXrefKey(cardNum));
  }

  readAll(): CardXrefRecord[] {
    return this.file.readAll();
  }

  /** CXACAIX path: every card cross reference for an account. */
  readByAccountId(acctId: number | string): CardXrefRecord[] {
    return this.file.readByAlternate(CXACAIX, String(acctId).trim().padStart(11, '0'));
  }

  write(record: CardXrefRecord): void {
    this.file.write(record);
  }

  rewrite(record: CardXrefRecord): void {
    this.file.rewrite(record);
  }

  delete(cardNum: string | number): void {
    this.file.delete(cardXrefKey(cardNum));
  }

  startBrowse(cardNum?: string | number) {
    return this.file.startBrowse(cardNum === undefined ? undefined : cardXrefKey(cardNum));
  }
}
