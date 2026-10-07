import type { Decimal } from 'decimal.js';
import { defineLayout } from './layout.js';

/** CVACT01Y – ACCOUNT-RECORD (ACCTDATA KSDS, RECLN 300, key ACCT-ID). */
export interface AccountRecord {
  /** ACCT-ID PIC 9(11) – record key */
  acctId: string;
  /** ACCT-ACTIVE-STATUS PIC X(01) */
  acctActiveStatus: string;
  /** ACCT-CURR-BAL PIC S9(10)V99 */
  acctCurrBal: Decimal;
  /** ACCT-CREDIT-LIMIT PIC S9(10)V99 */
  acctCreditLimit: Decimal;
  /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99 */
  acctCashCreditLimit: Decimal;
  /** ACCT-OPEN-DATE PIC X(10) */
  acctOpenDate: string;
  /** ACCT-EXPIRAION-DATE PIC X(10) (sic – spelling kept from the copybook) */
  acctExpirationDate: string;
  /** ACCT-REISSUE-DATE PIC X(10) */
  acctReissueDate: string;
  /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99 */
  acctCurrCycCredit: Decimal;
  /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99 */
  acctCurrCycDebit: Decimal;
  /** ACCT-ADDR-ZIP PIC X(10) */
  acctAddrZip: string;
  /** ACCT-GROUP-ID PIC X(10) */
  acctGroupId: string;
}

export const ACCOUNT_LAYOUT = defineLayout<AccountRecord>('ACCOUNT-RECORD (CVACT01Y)', [
  { kind: 'unsigned', name: 'acctId', length: 11 },
  { kind: 'alpha', name: 'acctActiveStatus', length: 1 },
  { kind: 'signed', name: 'acctCurrBal', intDigits: 10, scale: 2 },
  { kind: 'signed', name: 'acctCreditLimit', intDigits: 10, scale: 2 },
  { kind: 'signed', name: 'acctCashCreditLimit', intDigits: 10, scale: 2 },
  { kind: 'alpha', name: 'acctOpenDate', length: 10 },
  { kind: 'alpha', name: 'acctExpirationDate', length: 10 },
  { kind: 'alpha', name: 'acctReissueDate', length: 10 },
  { kind: 'signed', name: 'acctCurrCycCredit', intDigits: 10, scale: 2 },
  { kind: 'signed', name: 'acctCurrCycDebit', intDigits: 10, scale: 2 },
  { kind: 'alpha', name: 'acctAddrZip', length: 10 },
  { kind: 'alpha', name: 'acctGroupId', length: 10 },
  { kind: 'filler', length: 178 },
]);

export const accountKey = (r: AccountRecord): string => r.acctId;
