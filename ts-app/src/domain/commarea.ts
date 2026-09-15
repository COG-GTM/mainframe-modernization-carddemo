/**
 * COBOL copybook `app/cpy/COCOM01Y.cpy` — CARDDEMO-COMMAREA.
 *
 * In CICS this structure is passed from program to program on `EXEC CICS XCTL`.
 * Here it is the session state carried between HTTP requests, so the CICS
 * navigation fields (`CDEMO-FROM-*` / `CDEMO-TO-*`) become explicit route
 * bookkeeping rather than a program transfer.
 */
export interface CardDemoCommarea {
  /** CDEMO-FROM-TRANID PIC X(04) */
  fromTranid: string;
  /** CDEMO-FROM-PROGRAM PIC X(08) */
  fromProgram: string;
  /** CDEMO-TO-TRANID PIC X(04) */
  toTranid: string;
  /** CDEMO-TO-PROGRAM PIC X(08) */
  toProgram: string;
  /** CDEMO-USER-ID PIC X(08) */
  userId: string;
  /** CDEMO-USER-TYPE PIC X(01) — 88 CDEMO-USRTYP-ADMIN 'A' / CDEMO-USRTYP-USER 'U' */
  userType: string;
  /** CDEMO-PGM-CONTEXT PIC 9(01) — 0 = CDEMO-PGM-ENTER, 1 = CDEMO-PGM-REENTER */
  pgmContext: 0 | 1;
  /** CDEMO-CUST-ID PIC 9(09) */
  custId: string;
  /** CDEMO-CUST-FNAME PIC X(25) */
  custFname: string;
  /** CDEMO-CUST-MNAME PIC X(25) */
  custMname: string;
  /** CDEMO-CUST-LNAME PIC X(25) */
  custLname: string;
  /** CDEMO-ACCT-ID PIC 9(11) */
  acctId: string;
  /** CDEMO-ACCT-STATUS PIC X(01) */
  acctStatus: string;
  /** CDEMO-CARD-NUM PIC 9(16) */
  cardNum: string;
  /** CDEMO-LAST-MAP PIC X(7) */
  lastMap: string;
  /** CDEMO-LAST-MAPSET PIC X(7) */
  lastMapset: string;
}

export function emptyCommarea(): CardDemoCommarea {
  return {
    fromTranid: '',
    fromProgram: '',
    toTranid: '',
    toProgram: '',
    userId: '',
    userType: '',
    pgmContext: 0,
    custId: '',
    custFname: '',
    custMname: '',
    custLname: '',
    acctId: '',
    acctStatus: '',
    cardNum: '',
    lastMap: '',
    lastMapset: '',
  };
}

export function isAdmin(commarea: CardDemoCommarea): boolean {
  return commarea.userType === 'A';
}
