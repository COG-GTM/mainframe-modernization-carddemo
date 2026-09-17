import { ACCOUNT_LAYOUT, AccountRecord } from '../models/account';
import { KsdsFile } from './ksds';

/** ACCTDATA (MFE.CARDDEMO.ACCTDATA, CICS file ACCTDAT) keyed on ACCT-ID(11). */
export function accountKey(acctId: number | string): string {
  return String(acctId).trim().padStart(11, '0');
}

export class AccountRepository {
  readonly file = new KsdsFile<AccountRecord>({
    name: 'ACCTDAT',
    layout: ACCOUNT_LAYOUT,
    keyOf: (record) => accountKey(record.acctId),
  });

  load(records: Iterable<AccountRecord>): void {
    this.file.load(records);
  }

  read(acctId: number | string): AccountRecord {
    return this.file.read(accountKey(acctId));
  }

  tryRead(acctId: number | string): AccountRecord | undefined {
    return this.file.tryRead(accountKey(acctId));
  }

  readAll(): AccountRecord[] {
    return this.file.readAll();
  }

  write(record: AccountRecord): void {
    this.file.write(record);
  }

  rewrite(record: AccountRecord): void {
    this.file.rewrite(record);
  }

  delete(acctId: number | string): void {
    this.file.delete(accountKey(acctId));
  }

  startBrowse(acctId?: number | string) {
    return this.file.startBrowse(acctId === undefined ? undefined : accountKey(acctId));
  }
}
