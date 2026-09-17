/**
 * Generated from app/cpy/CSUSR01Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 80 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface SecurityUserRecord {
  /** SEC-USR-ID PIC X(08), bytes 1-8 */
  secUsrId: string;
  /** SEC-USR-FNAME PIC X(20), bytes 9-28 */
  secUsrFname: string;
  /** SEC-USR-LNAME PIC X(20), bytes 29-48 */
  secUsrLname: string;
  /** SEC-USR-PWD PIC X(08), bytes 49-56 */
  secUsrPwd: string;
  /** SEC-USR-TYPE PIC X(01), bytes 57-57 */
  secUsrType: string;
  /** SEC-USR-FILLER PIC X(23), bytes 58-80 */
  secUsrFiller: string;
}

export const SECURITY_USER_RECORD_LENGTH = 80;

export const SECURITY_USER_RECORD_SPEC: RecordSpec<SecurityUserRecord> = {
  name: 'SecurityUserRecord',
  copybook: 'app/cpy/CSUSR01Y.cpy',
  recordLength: 80,
  fields: [
    {
      name: 'secUsrId',
      cobolName: 'SEC-USR-ID',
      offset: 0,
      length: 8,
      filler: false,
      picture: {
        pic: 'X(08)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 8,
      },
    },
    {
      name: 'secUsrFname',
      cobolName: 'SEC-USR-FNAME',
      offset: 8,
      length: 20,
      filler: false,
      picture: {
        pic: 'X(20)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 20,
      },
    },
    {
      name: 'secUsrLname',
      cobolName: 'SEC-USR-LNAME',
      offset: 28,
      length: 20,
      filler: false,
      picture: {
        pic: 'X(20)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 20,
      },
    },
    {
      name: 'secUsrPwd',
      cobolName: 'SEC-USR-PWD',
      offset: 48,
      length: 8,
      filler: false,
      picture: {
        pic: 'X(08)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 8,
      },
    },
    {
      name: 'secUsrType',
      cobolName: 'SEC-USR-TYPE',
      offset: 56,
      length: 1,
      filler: false,
      picture: {
        pic: 'X(01)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 1,
      },
    },
    {
      name: 'secUsrFiller',
      cobolName: 'SEC-USR-FILLER',
      offset: 57,
      length: 23,
      filler: false,
      picture: {
        pic: 'X(23)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 23,
      },
    },
  ],
};
