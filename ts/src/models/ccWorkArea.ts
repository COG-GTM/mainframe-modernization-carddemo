/**
 * CC-WORK-AREAS (copybook CVCRD01Y) — the per-task working area shared by the
 * card/account online programs, plus the AID (attention identifier) 88-levels
 * that CSSTRPFY.cpy sets from EIBAID.
 */

export const AID_ENTER = 'ENTER';
export const AID_CLEAR = 'CLEAR';
export const AID_PA1 = 'PA1  ';
export const AID_PA2 = 'PA2  ';
export const AID_PFK01 = 'PFK01';
export const AID_PFK02 = 'PFK02';
export const AID_PFK03 = 'PFK03';
export const AID_PFK04 = 'PFK04';
export const AID_PFK05 = 'PFK05';
export const AID_PFK06 = 'PFK06';
export const AID_PFK07 = 'PFK07';
export const AID_PFK08 = 'PFK08';
export const AID_PFK09 = 'PFK09';
export const AID_PFK10 = 'PFK10';
export const AID_PFK11 = 'PFK11';
export const AID_PFK12 = 'PFK12';

export type AidKey =
  | typeof AID_ENTER
  | typeof AID_CLEAR
  | typeof AID_PA1
  | typeof AID_PA2
  | typeof AID_PFK01
  | typeof AID_PFK02
  | typeof AID_PFK03
  | typeof AID_PFK04
  | typeof AID_PFK05
  | typeof AID_PFK06
  | typeof AID_PFK07
  | typeof AID_PFK08
  | typeof AID_PFK09
  | typeof AID_PFK10
  | typeof AID_PFK11
  | typeof AID_PFK12;

/**
 * CSSTRPFY.cpy maps PF13-PF24 onto PF1-PF12; this reproduces that folding for a
 * raw key name such as 'PF15'.
 */
export function aidFromKeyName(keyName: string): AidKey {
  const upper = keyName.trim().toUpperCase();
  switch (upper) {
    case 'ENTER':
      return AID_ENTER;
    case 'CLEAR':
      return AID_CLEAR;
    case 'PA1':
      return AID_PA1;
    case 'PA2':
      return AID_PA2;
    default:
      break;
  }
  const match = /^PF(\d{1,2})$/.exec(upper);
  if (match) {
    const folded = ((Number(match[1]) - 1) % 12) + 1;
    return `PFK${String(folded).padStart(2, '0')}` as AidKey;
  }
  return AID_ENTER;
}

export interface CcWorkArea {
  /** CCARD-AID */
  ccardAid: AidKey;
  /** CCARD-NEXT-PROG */
  ccardNextProg: string;
  /** CCARD-NEXT-MAPSET */
  ccardNextMapset: string;
  /** CCARD-NEXT-MAP */
  ccardNextMap: string;
  /** CCARD-ERROR-MSG, PIC X(75) */
  ccardErrorMsg: string;
  /** CCARD-RETURN-MSG, PIC X(75) */
  ccardReturnMsg: string;
  /** CC-ACCT-ID, PIC X(11) redefined as 9(11) */
  ccAcctId: string;
  /** CC-CARD-NUM, PIC X(16) redefined as 9(16) */
  ccCardNum: string;
  /** CC-CUST-ID, PIC X(09) redefined as 9(09) */
  ccCustId: string;
}

export function emptyCcWorkArea(): CcWorkArea {
  return {
    ccardAid: AID_ENTER,
    ccardNextProg: '',
    ccardNextMapset: '',
    ccardNextMap: '',
    ccardErrorMsg: '',
    ccardReturnMsg: '',
    ccAcctId: '',
    ccCardNum: '',
    ccCustId: '',
  };
}
