import { SEC_USER_LAYOUT, SecUserRecord } from '../models/user';
import { KsdsFile } from './ksds';

/** USRSEC (MFE.CARDDEMO.USRSEC.VSAM.KSDS, CICS file USRSEC) keyed on SEC-USR-ID(8). */
export function userKey(userId: string): string {
  return userId.trim().padEnd(8, ' ');
}

export class UserSecurityRepository {
  readonly file = new KsdsFile<SecUserRecord>({
    name: 'USRSEC',
    layout: SEC_USER_LAYOUT,
    keyOf: (record) => userKey(record.secUsrId),
  });

  load(records: Iterable<SecUserRecord>): void {
    this.file.load(records);
  }

  read(userId: string): SecUserRecord {
    return this.file.read(userKey(userId));
  }

  tryRead(userId: string): SecUserRecord | undefined {
    return this.file.tryRead(userKey(userId));
  }

  readAll(): SecUserRecord[] {
    return this.file.readAll();
  }

  write(record: SecUserRecord): void {
    this.file.write(record);
  }

  rewrite(record: SecUserRecord): void {
    this.file.rewrite(record);
  }

  delete(userId: string): void {
    this.file.delete(userKey(userId));
  }

  startBrowse(userId?: string) {
    return this.file.startBrowse(userId === undefined ? undefined : userKey(userId));
  }
}
