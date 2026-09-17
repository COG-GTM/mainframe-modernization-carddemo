import {
  DISCLOSURE_GROUP_LAYOUT,
  DisclosureGroupRecord,
  disclosureGroupKey,
} from '../models/disclosureGroup';
import { KsdsFile } from './ksds';

/** DISCGRP (MFE.CARDDEMO.DISCGRP, CICS file DISCGRP) keyed on DIS-GROUP-KEY(16). */
export class DisclosureGroupRepository {
  readonly file = new KsdsFile<DisclosureGroupRecord>({
    name: 'DISCGRP',
    layout: DISCLOSURE_GROUP_LAYOUT,
    keyOf: disclosureGroupKey,
  });

  load(records: Iterable<DisclosureGroupRecord>): void {
    this.file.load(records);
  }

  read(groupId: string, tranTypeCd: string, tranCatCd: number): DisclosureGroupRecord {
    return this.file.read(
      disclosureGroupKey({
        disAcctGroupId: groupId,
        disTranTypeCd: tranTypeCd,
        disTranCatCd: tranCatCd,
      }),
    );
  }

  tryRead(
    groupId: string,
    tranTypeCd: string,
    tranCatCd: number,
  ): DisclosureGroupRecord | undefined {
    return this.file.tryRead(
      disclosureGroupKey({
        disAcctGroupId: groupId,
        disTranTypeCd: tranTypeCd,
        disTranCatCd: tranCatCd,
      }),
    );
  }

  readAll(): DisclosureGroupRecord[] {
    return this.file.readAll();
  }

  write(record: DisclosureGroupRecord): void {
    this.file.write(record);
  }

  rewrite(record: DisclosureGroupRecord): void {
    this.file.rewrite(record);
  }
}
