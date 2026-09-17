import { RecordLayout } from './layout';

/**
 * SEC-USER-DATA (copybook CSUSR01Y, LRECL 80, dataset USRSEC).
 * UNUSED1Y.cpy declares the identical layout under UNUSED-* names.
 *
 * off  len  pic        field
 *   0    8  X(08)      SEC-USR-ID
 *   8   20  X(20)      SEC-USR-FNAME
 *  28   20  X(20)      SEC-USR-LNAME
 *  48    8  X(08)      SEC-USR-PWD
 *  56    1  X(01)      SEC-USR-TYPE ('A' admin, 'U' user)
 *  57   23  X(23)      SEC-USR-FILLER
 */
export interface SecUserRecord {
  secUsrId: string;
  secUsrFname: string;
  secUsrLname: string;
  secUsrPwd: string;
  secUsrType: string;
}

export const SEC_USER_RECORD_LENGTH = 80;

export const USER_TYPE_ADMIN = 'A';
export const USER_TYPE_USER = 'U';

export const SEC_USER_LAYOUT: RecordLayout<SecUserRecord> = {
  copybook: 'CSUSR01Y',
  length: SEC_USER_RECORD_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'secUsrId', length: 8 },
    { kind: 'alphanumeric', name: 'secUsrFname', length: 20 },
    { kind: 'alphanumeric', name: 'secUsrLname', length: 20 },
    { kind: 'alphanumeric', name: 'secUsrPwd', length: 8 },
    { kind: 'alphanumeric', name: 'secUsrType', length: 1 },
    { kind: 'filler', name: 'secUsrFiller', length: 23 },
  ],
  fromFields: (v) => ({
    secUsrId: v.secUsrId as string,
    secUsrFname: v.secUsrFname as string,
    secUsrLname: v.secUsrLname as string,
    secUsrPwd: v.secUsrPwd as string,
    secUsrType: v.secUsrType as string,
  }),
  toFields: (r) => ({ ...r }),
};
