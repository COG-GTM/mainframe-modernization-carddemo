/**
 * COCOM01Y – CARDDEMO-COMMAREA.
 *
 * CICS communication area shared by the online programs. CBTRN02C is a batch
 * program and does not use it; the type is provided so online ports can share
 * the same copybook module.
 */
export type CdemoUserType = 'A' | 'U';
export type CdemoPgmContext = 0 | 1;

export interface CardDemoCommarea {
  general: {
    /** CDEMO-FROM-TRANID PIC X(04) */
    fromTranId: string;
    /** CDEMO-FROM-PROGRAM PIC X(08) */
    fromProgram: string;
    /** CDEMO-TO-TRANID PIC X(04) */
    toTranId: string;
    /** CDEMO-TO-PROGRAM PIC X(08) */
    toProgram: string;
    /** CDEMO-USER-ID PIC X(08) */
    userId: string;
    /** CDEMO-USER-TYPE PIC X(01): 88 ADMIN 'A', 88 USER 'U' */
    userType: CdemoUserType;
    /** CDEMO-PGM-CONTEXT PIC 9(01): 88 ENTER 0, 88 REENTER 1 */
    pgmContext: CdemoPgmContext;
  };
  customer: {
    /** CDEMO-CUST-ID PIC 9(09) */
    custId: string;
    /** CDEMO-CUST-FNAME PIC X(25) */
    custFname: string;
    /** CDEMO-CUST-MNAME PIC X(25) */
    custMname: string;
    /** CDEMO-CUST-LNAME PIC X(25) */
    custLname: string;
  };
  account: {
    /** CDEMO-ACCT-ID PIC 9(11) */
    acctId: string;
    /** CDEMO-ACCT-STATUS PIC X(01) */
    acctStatus: string;
  };
  card: {
    /** CDEMO-CARD-NUM PIC 9(16) */
    cardNum: string;
  };
  more: {
    /** CDEMO-LAST-MAP PIC X(7) */
    lastMap: string;
    /** CDEMO-LAST-MAPSET PIC X(7) */
    lastMapset: string;
  };
}
