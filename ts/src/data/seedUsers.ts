import { SecUserRecord } from '../models/user';

/**
 * Initial USRSEC content. The mainframe loads it from the in-stream SYSUT1 data
 * of DUSRSECJ.jcl (IEBGENER), so it is reproduced here verbatim.
 */
export const SEED_USERS: readonly SecUserRecord[] = [
  {
    secUsrId: 'ADMIN001',
    secUsrFname: 'MARGARET',
    secUsrLname: 'GOLD',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'A',
  },
  {
    secUsrId: 'ADMIN002',
    secUsrFname: 'RUSSELL',
    secUsrLname: 'RUSSELL',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'A',
  },
  {
    secUsrId: 'ADMIN003',
    secUsrFname: 'RAYMOND',
    secUsrLname: 'WHITMORE',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'A',
  },
  {
    secUsrId: 'ADMIN004',
    secUsrFname: 'EMMANUEL',
    secUsrLname: 'CASGRAIN',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'A',
  },
  {
    secUsrId: 'ADMIN005',
    secUsrFname: 'GRANVILLE',
    secUsrLname: 'LACHAPELLE',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'A',
  },
  {
    secUsrId: 'USER0001',
    secUsrFname: 'LAWRENCE',
    secUsrLname: 'THOMAS',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'U',
  },
  {
    secUsrId: 'USER0002',
    secUsrFname: 'AJITH',
    secUsrLname: 'KUMAR',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'U',
  },
  {
    secUsrId: 'USER0003',
    secUsrFname: 'LAURITZ',
    secUsrLname: 'ALME',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'U',
  },
  {
    secUsrId: 'USER0004',
    secUsrFname: 'AVERARDO',
    secUsrLname: 'MAZZI',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'U',
  },
  {
    secUsrId: 'USER0005',
    secUsrFname: 'LEE',
    secUsrLname: 'TING',
    secUsrPwd: 'PASSWORD',
    secUsrType: 'U',
  },
];
