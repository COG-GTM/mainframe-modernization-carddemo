import type { Decimal } from 'decimal.js';
import { alnum, digits, filler, signed, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CVACT01Y.cpy` — ACCOUNT-RECORD (RECLN 300). */
export interface AccountRecord {
  /** ACCT-ID PIC 9(11) */
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
  /** ACCT-EXPIRAION-DATE PIC X(10) (COBOL spelling preserved) */
  acctExpiraionDate: string;
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

export const ACCOUNT_LAYOUT: RecordLayout<AccountRecord> = {
  copybook: 'CVACT01Y',
  recordLength: 300,
  fields: [
    digits('acctId', 11),
    alnum('acctActiveStatus', 1),
    signed('acctCurrBal', 12, 2),
    signed('acctCreditLimit', 12, 2),
    signed('acctCashCreditLimit', 12, 2),
    alnum('acctOpenDate', 10),
    alnum('acctExpiraionDate', 10),
    alnum('acctReissueDate', 10),
    signed('acctCurrCycCredit', 12, 2),
    signed('acctCurrCycDebit', 12, 2),
    alnum('acctAddrZip', 10),
    alnum('acctGroupId', 10),
    filler(178),
  ],
};
