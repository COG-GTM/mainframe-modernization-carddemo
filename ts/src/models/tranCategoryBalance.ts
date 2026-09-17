import { RecordLayout } from './layout';

/**
 * TRAN-CAT-BAL-RECORD (copybook CVTRA01Y, LRECL 50, dataset TCATBALF).
 * The KSDS key is the 17 byte TRAN-CAT-KEY (acct id + type cd + cat cd).
 *
 * off  len  pic          field
 *   0   11  9(11)        TRANCAT-ACCT-ID  \
 *  11    2  X(02)        TRANCAT-TYPE-CD   > TRAN-CAT-KEY
 *  13    4  9(04)        TRANCAT-CD       /
 *  17   11  S9(09)V99    TRAN-CAT-BAL
 *  28   22  X(22)        FILLER
 */
export interface TranCatBalRecord {
  trancatAcctId: number;
  trancatTypeCd: string;
  trancatCd: number;
  tranCatBal: number;
}

export const TRAN_CAT_BAL_RECORD_LENGTH = 50;

export const TRAN_CAT_BAL_LAYOUT: RecordLayout<TranCatBalRecord> = {
  copybook: 'CVTRA01Y',
  length: TRAN_CAT_BAL_RECORD_LENGTH,
  fields: [
    { kind: 'unsigned', name: 'trancatAcctId', length: 11 },
    { kind: 'alphanumeric', name: 'trancatTypeCd', length: 2 },
    { kind: 'unsigned', name: 'trancatCd', length: 4 },
    { kind: 'signed', name: 'tranCatBal', length: 11, scale: 2 },
    { kind: 'filler', length: 22 },
  ],
  fromFields: (v) => ({
    trancatAcctId: v.trancatAcctId as number,
    trancatTypeCd: v.trancatTypeCd as string,
    trancatCd: v.trancatCd as number,
    tranCatBal: v.tranCatBal as number,
  }),
  toFields: (r) => ({ ...r }),
};

/** TRAN-CAT-KEY as the 17 byte string used for keyed reads. */
export function tranCatBalKey(record: {
  trancatAcctId: number;
  trancatTypeCd: string;
  trancatCd: number;
}): string {
  return (
    String(record.trancatAcctId).padStart(11, '0') +
    record.trancatTypeCd.padEnd(2, ' ') +
    String(record.trancatCd).padStart(4, '0')
  );
}
