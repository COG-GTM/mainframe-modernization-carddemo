import { RecordLayout } from './layout';

/**
 * CARDDEMO-COMMAREA (copybook COCOM01Y, 160 bytes) — the state carried between
 * pseudo-conversational CICS tasks via `RETURN TRANSID ... COMMAREA`.
 *
 * off  len  pic        field
 *   0    4  X(04)      CDEMO-FROM-TRANID
 *   4    8  X(08)      CDEMO-FROM-PROGRAM
 *  12    4  X(04)      CDEMO-TO-TRANID
 *  16    8  X(08)      CDEMO-TO-PROGRAM
 *  24    8  X(08)      CDEMO-USER-ID
 *  32    1  X(01)      CDEMO-USER-TYPE ('A' admin, 'U' user)
 *  33    1  9(01)      CDEMO-PGM-CONTEXT (0 enter, 1 re-enter)
 *  34    9  9(09)      CDEMO-CUST-ID
 *  43   25  X(25)      CDEMO-CUST-FNAME
 *  68   25  X(25)      CDEMO-CUST-MNAME
 *  93   25  X(25)      CDEMO-CUST-LNAME
 * 118   11  9(11)      CDEMO-ACCT-ID
 * 129    1  X(01)      CDEMO-ACCT-STATUS
 * 130   16  9(16)      CDEMO-CARD-NUM
 * 146    7  X(07)      CDEMO-LAST-MAP
 * 153    7  X(07)      CDEMO-LAST-MAPSET
 */
export interface CardDemoCommarea {
  cdemoFromTranid: string;
  cdemoFromProgram: string;
  cdemoToTranid: string;
  cdemoToProgram: string;
  cdemoUserId: string;
  cdemoUserType: string;
  cdemoPgmContext: number;
  cdemoCustId: number;
  cdemoCustFname: string;
  cdemoCustMname: string;
  cdemoCustLname: string;
  cdemoAcctId: number;
  cdemoAcctStatus: string;
  cdemoCardNum: number;
  cdemoLastMap: string;
  cdemoLastMapset: string;
}

export const COMMAREA_LENGTH = 160;

/** 88-level CDEMO-USRTYP-ADMIN / CDEMO-USRTYP-USER. */
export const CDEMO_USRTYP_ADMIN = 'A';
export const CDEMO_USRTYP_USER = 'U';

/** 88-level CDEMO-PGM-ENTER / CDEMO-PGM-REENTER. */
export const CDEMO_PGM_ENTER = 0;
export const CDEMO_PGM_REENTER = 1;

export const COMMAREA_LAYOUT: RecordLayout<CardDemoCommarea> = {
  copybook: 'COCOM01Y',
  length: COMMAREA_LENGTH,
  fields: [
    { kind: 'alphanumeric', name: 'cdemoFromTranid', length: 4 },
    { kind: 'alphanumeric', name: 'cdemoFromProgram', length: 8 },
    { kind: 'alphanumeric', name: 'cdemoToTranid', length: 4 },
    { kind: 'alphanumeric', name: 'cdemoToProgram', length: 8 },
    { kind: 'alphanumeric', name: 'cdemoUserId', length: 8 },
    { kind: 'alphanumeric', name: 'cdemoUserType', length: 1 },
    { kind: 'unsigned', name: 'cdemoPgmContext', length: 1 },
    { kind: 'unsigned', name: 'cdemoCustId', length: 9 },
    { kind: 'alphanumeric', name: 'cdemoCustFname', length: 25 },
    { kind: 'alphanumeric', name: 'cdemoCustMname', length: 25 },
    { kind: 'alphanumeric', name: 'cdemoCustLname', length: 25 },
    { kind: 'unsigned', name: 'cdemoAcctId', length: 11 },
    { kind: 'alphanumeric', name: 'cdemoAcctStatus', length: 1 },
    { kind: 'unsigned', name: 'cdemoCardNum', length: 16 },
    { kind: 'alphanumeric', name: 'cdemoLastMap', length: 7 },
    { kind: 'alphanumeric', name: 'cdemoLastMapset', length: 7 },
  ],
  fromFields: (v) => ({
    cdemoFromTranid: v.cdemoFromTranid as string,
    cdemoFromProgram: v.cdemoFromProgram as string,
    cdemoToTranid: v.cdemoToTranid as string,
    cdemoToProgram: v.cdemoToProgram as string,
    cdemoUserId: v.cdemoUserId as string,
    cdemoUserType: v.cdemoUserType as string,
    cdemoPgmContext: v.cdemoPgmContext as number,
    cdemoCustId: v.cdemoCustId as number,
    cdemoCustFname: v.cdemoCustFname as string,
    cdemoCustMname: v.cdemoCustMname as string,
    cdemoCustLname: v.cdemoCustLname as string,
    cdemoAcctId: v.cdemoAcctId as number,
    cdemoAcctStatus: v.cdemoAcctStatus as string,
    cdemoCardNum: v.cdemoCardNum as number,
    cdemoLastMap: v.cdemoLastMap as string,
    cdemoLastMapset: v.cdemoLastMapset as string,
  }),
  toFields: (r) => ({ ...r }),
};

/** A COMMAREA initialised to LOW-VALUES/spaces, as CICS passes on first entry. */
export function emptyCommarea(): CardDemoCommarea {
  return {
    cdemoFromTranid: '',
    cdemoFromProgram: '',
    cdemoToTranid: '',
    cdemoToProgram: '',
    cdemoUserId: '',
    cdemoUserType: '',
    cdemoPgmContext: CDEMO_PGM_ENTER,
    cdemoCustId: 0,
    cdemoCustFname: '',
    cdemoCustMname: '',
    cdemoCustLname: '',
    cdemoAcctId: 0,
    cdemoAcctStatus: '',
    cdemoCardNum: 0,
    cdemoLastMap: '',
    cdemoLastMapset: '',
  };
}

export function isAdmin(commarea: Pick<CardDemoCommarea, 'cdemoUserType'>): boolean {
  return commarea.cdemoUserType === CDEMO_USRTYP_ADMIN;
}
