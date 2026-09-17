import { RecordLayout } from './layout';

/**
 * ACCOUNT-RECORD (copybook CVACT01Y, LRECL 300, dataset ACCTDATA).
 *
 * off  len  pic              field
 *   0   11  9(11)            ACCT-ID
 *  11    1  X(01)            ACCT-ACTIVE-STATUS
 *  12   12  S9(10)V99        ACCT-CURR-BAL
 *  24   12  S9(10)V99        ACCT-CREDIT-LIMIT
 *  36   12  S9(10)V99        ACCT-CASH-CREDIT-LIMIT
 *  48   10  X(10)            ACCT-OPEN-DATE
 *  58   10  X(10)            ACCT-EXPIRAION-DATE
 *  68   10  X(10)            ACCT-REISSUE-DATE
 *  78   12  S9(10)V99        ACCT-CURR-CYC-CREDIT
 *  90   12  S9(10)V99        ACCT-CURR-CYC-DEBIT
 * 102   10  X(10)            ACCT-ADDR-ZIP
 * 112   10  X(10)            ACCT-GROUP-ID
 * 122  178  X(178)           FILLER
 */
export interface AccountRecord {
  acctId: number;
  acctActiveStatus: string;
  acctCurrBal: number;
  acctCreditLimit: number;
  acctCashCreditLimit: number;
  acctOpenDate: string;
  acctExpiraionDate: string;
  acctReissueDate: string;
  acctCurrCycCredit: number;
  acctCurrCycDebit: number;
  acctAddrZip: string;
  acctGroupId: string;
}

export const ACCOUNT_RECORD_LENGTH = 300;

export const ACCOUNT_LAYOUT: RecordLayout<AccountRecord> = {
  copybook: 'CVACT01Y',
  length: ACCOUNT_RECORD_LENGTH,
  fields: [
    { kind: 'unsigned', name: 'acctId', length: 11 },
    { kind: 'alphanumeric', name: 'acctActiveStatus', length: 1 },
    { kind: 'signed', name: 'acctCurrBal', length: 12, scale: 2 },
    { kind: 'signed', name: 'acctCreditLimit', length: 12, scale: 2 },
    { kind: 'signed', name: 'acctCashCreditLimit', length: 12, scale: 2 },
    { kind: 'alphanumeric', name: 'acctOpenDate', length: 10 },
    { kind: 'alphanumeric', name: 'acctExpiraionDate', length: 10 },
    { kind: 'alphanumeric', name: 'acctReissueDate', length: 10 },
    { kind: 'signed', name: 'acctCurrCycCredit', length: 12, scale: 2 },
    { kind: 'signed', name: 'acctCurrCycDebit', length: 12, scale: 2 },
    { kind: 'alphanumeric', name: 'acctAddrZip', length: 10 },
    { kind: 'alphanumeric', name: 'acctGroupId', length: 10 },
    { kind: 'filler', length: 178 },
  ],
  fromFields: (v) => ({
    acctId: v.acctId as number,
    acctActiveStatus: v.acctActiveStatus as string,
    acctCurrBal: v.acctCurrBal as number,
    acctCreditLimit: v.acctCreditLimit as number,
    acctCashCreditLimit: v.acctCashCreditLimit as number,
    acctOpenDate: v.acctOpenDate as string,
    acctExpiraionDate: v.acctExpiraionDate as string,
    acctReissueDate: v.acctReissueDate as string,
    acctCurrCycCredit: v.acctCurrCycCredit as number,
    acctCurrCycDebit: v.acctCurrCycDebit as number,
    acctAddrZip: v.acctAddrZip as string,
    acctGroupId: v.acctGroupId as string,
  }),
  toFields: (r) => ({ ...r }),
};
