import { RecordLayout } from './layout';

/**
 * DIS-GROUP-RECORD (copybook CVTRA02Y, LRECL 50, dataset DISCGRP).
 * The KSDS key is the 16 byte DIS-GROUP-KEY (group id + type cd + cat cd).
 *
 * off  len  pic          field
 *   0   10  X(10)        DIS-ACCT-GROUP-ID \
 *  10    2  X(02)        DIS-TRAN-TYPE-CD   > DIS-GROUP-KEY
 *  12    4  9(04)        DIS-TRAN-CAT-CD   /
 *  16    6  S9(04)V99    DIS-INT-RATE
 *  22   28  X(28)        FILLER
 */
export interface DisclosureGroupRecord {
  disAcctGroupId: string;
  disTranTypeCd: string;
  disTranCatCd: number;
  disIntRate: number;
}

export const DISCLOSURE_GROUP_RECORD_LENGTH = 50;

export const DISCLOSURE_GROUP_LAYOUT: RecordLayout<DisclosureGroupRecord> = {
  copybook: 'CVTRA02Y',
  length: DISCLOSURE_GROUP_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'disAcctGroupId', length: 10 },
    { kind: 'alphanumeric', name: 'disTranTypeCd', length: 2 },
    { kind: 'unsigned', name: 'disTranCatCd', length: 4 },
    { kind: 'signed', name: 'disIntRate', length: 6, scale: 2 },
    { kind: 'filler', length: 28 },
  ],
  fromFields: (v) => ({
    disAcctGroupId: v.disAcctGroupId as string,
    disTranTypeCd: v.disTranTypeCd as string,
    disTranCatCd: v.disTranCatCd as number,
    disIntRate: v.disIntRate as number,
  }),
  toFields: (r) => ({ ...r }),
};

export function disclosureGroupKey(record: {
  disAcctGroupId: string;
  disTranTypeCd: string;
  disTranCatCd: number;
}): string {
  return (
    record.disAcctGroupId.padEnd(10, ' ') +
    record.disTranTypeCd.padEnd(2, ' ') +
    String(record.disTranCatCd).padStart(4, '0')
  );
}
