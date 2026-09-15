import { alnum, filler, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CSUSR01Y.cpy` — SEC-USER-DATA (LRECL 80). */
export interface SecUserRecord {
  /** SEC-USR-ID PIC X(08) */
  secUsrId: string;
  /** SEC-USR-FNAME PIC X(20) */
  secUsrFname: string;
  /** SEC-USR-LNAME PIC X(20) */
  secUsrLname: string;
  /** SEC-USR-PWD PIC X(08) */
  secUsrPwd: string;
  /** SEC-USR-TYPE PIC X(01) — 'A' admin, 'U' regular user */
  secUsrType: string;
}

export const USER_TYPE_ADMIN = 'A';
export const USER_TYPE_USER = 'U';

export const SEC_USER_LAYOUT: RecordLayout<SecUserRecord> = {
  copybook: 'CSUSR01Y',
  recordLength: 80,
  fields: [
    alnum('secUsrId', 8),
    alnum('secUsrFname', 20),
    alnum('secUsrLname', 20),
    alnum('secUsrPwd', 8),
    alnum('secUsrType', 1),
    filler(23),
  ],
};
