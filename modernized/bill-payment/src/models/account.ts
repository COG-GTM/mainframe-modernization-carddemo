import { Money } from '../money';

/**
 * Account entity — modern equivalent of copybook `CVACT01Y` (VSAM file ACCTDAT,
 * record length 300). Field comments show the original COBOL PIC clause.
 */
export interface Account {
  /** ACCT-ID            PIC 9(11) — 11-digit key, kept as string to preserve leading zeros */
  acctId: string;
  /** ACCT-ACTIVE-STATUS PIC X(01) — 'Y' active / 'N' inactive */
  activeStatus: string;
  /** ACCT-CURR-BAL      PIC S9(10)V99 */
  currentBalance: Money;
  /** ACCT-CREDIT-LIMIT  PIC S9(10)V99 */
  creditLimit: Money;
  /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99 */
  cashCreditLimit: Money;
  /** ACCT-OPEN-DATE     PIC X(10) */
  openDate: string;
  /** ACCT-EXPIRAION-DATE PIC X(10) (original copybook spelling preserved) */
  expirationDate: string;
  /** ACCT-REISSUE-DATE  PIC X(10) */
  reissueDate: string;
  /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99 */
  currentCycleCredit: Money;
  /** ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 */
  currentCycleDebit: Money;
  /** ACCT-ADDR-ZIP     PIC X(10) */
  addressZip: string;
  /** ACCT-GROUP-ID     PIC X(10) */
  groupId: string;
}

/** Account ID is an 11-digit numeric key (`PIC 9(11)`). */
export function isValidAcctId(acctId: string): boolean {
  return /^\d{11}$/.test(acctId);
}

/** Normalize a user-supplied account id to the fixed 11-digit form. */
export function normalizeAcctId(raw: string): string {
  return raw.trim().padStart(11, '0');
}
