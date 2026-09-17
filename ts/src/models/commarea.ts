/**
 * Migrated from app/cpy/COCOM01Y.cpy (CARDDEMO-COMMAREA).
 *
 * In CICS this 300-byte COMMAREA is passed between pseudo-conversational
 * programs on XCTL/RETURN. In the TypeScript port it is server-side session
 * state, so it is modelled as a plain object rather than a fixed-length record.
 */

export type UserType = 'A' | 'U';

/** CDEMO-PGM-CONTEXT: 0 = first entry into the program, 1 = re-entry. */
export enum ProgramContext {
  Enter = 0,
  Reenter = 1,
}

export interface CardDemoGeneralInfo {
  fromTranId: string;
  fromProgram: string;
  toTranId: string;
  toProgram: string;
  userId: string;
  userType: UserType | '';
  pgmContext: ProgramContext;
}

export interface CardDemoCustomerInfo {
  custId: number;
  custFname: string;
  custMname: string;
  custLname: string;
}

export interface CardDemoAccountInfo {
  acctId: number;
  acctStatus: string;
}

export interface CardDemoCardInfo {
  cardNum: string;
}

export interface CardDemoMoreInfo {
  lastMap: string;
  lastMapset: string;
}

export interface CardDemoCommarea {
  general: CardDemoGeneralInfo;
  customer: CardDemoCustomerInfo;
  account: CardDemoAccountInfo;
  card: CardDemoCardInfo;
  more: CardDemoMoreInfo;
}

export function isAdmin(commarea: CardDemoCommarea): boolean {
  return commarea.general.userType === 'A';
}

export function createCommarea(): CardDemoCommarea {
  return {
    general: {
      fromTranId: '',
      fromProgram: '',
      toTranId: '',
      toProgram: '',
      userId: '',
      userType: '',
      pgmContext: ProgramContext.Enter,
    },
    customer: { custId: 0, custFname: '', custMname: '', custLname: '' },
    account: { acctId: 0, acctStatus: '' },
    card: { cardNum: '' },
    more: { lastMap: '', lastMapset: '' },
  };
}
