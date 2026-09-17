/**
 * Generated from app/cpy/CVACT01Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 300 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface AccountRecord {
  /** ACCT-ID PIC 9(11), bytes 1-11 */
  acctId: number;
  /** ACCT-ACTIVE-STATUS PIC X(01), bytes 12-12 */
  acctActiveStatus: string;
  /** ACCT-CURR-BAL PIC S9(10)V99, bytes 13-24 */
  acctCurrBal: number;
  /** ACCT-CREDIT-LIMIT PIC S9(10)V99, bytes 25-36 */
  acctCreditLimit: number;
  /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99, bytes 37-48 */
  acctCashCreditLimit: number;
  /** ACCT-OPEN-DATE PIC X(10), bytes 49-58 */
  acctOpenDate: string;
  /** ACCT-EXPIRAION-DATE PIC X(10), bytes 59-68 */
  acctExpiraionDate: string;
  /** ACCT-REISSUE-DATE PIC X(10), bytes 69-78 */
  acctReissueDate: string;
  /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99, bytes 79-90 */
  acctCurrCycCredit: number;
  /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99, bytes 91-102 */
  acctCurrCycDebit: number;
  /** ACCT-ADDR-ZIP PIC X(10), bytes 103-112 */
  acctAddrZip: string;
  /** ACCT-GROUP-ID PIC X(10), bytes 113-122 */
  acctGroupId: string;
}

export const ACCOUNT_RECORD_LENGTH = 300;

export const ACCOUNT_RECORD_SPEC: RecordSpec<AccountRecord> = {
  name: 'AccountRecord',
  copybook: 'app/cpy/CVACT01Y.cpy',
  recordLength: 300,
  fields: [
    {
      name: 'acctId',
      cobolName: 'ACCT-ID',
      offset: 0,
      length: 11,
      filler: false,
      picture: {
        pic: '9(11)',
        kind: 'numeric',
        digits: 11,
        scale: 0,
        signed: false,
        packed: false,
        length: 11,
      },
    },
    {
      name: 'acctActiveStatus',
      cobolName: 'ACCT-ACTIVE-STATUS',
      offset: 11,
      length: 1,
      filler: false,
      picture: {
        pic: 'X(01)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 1,
      },
    },
    {
      name: 'acctCurrBal',
      cobolName: 'ACCT-CURR-BAL',
      offset: 12,
      length: 12,
      filler: false,
      picture: {
        pic: 'S9(10)V99',
        kind: 'numeric',
        digits: 12,
        scale: 2,
        signed: true,
        packed: false,
        length: 12,
      },
    },
    {
      name: 'acctCreditLimit',
      cobolName: 'ACCT-CREDIT-LIMIT',
      offset: 24,
      length: 12,
      filler: false,
      picture: {
        pic: 'S9(10)V99',
        kind: 'numeric',
        digits: 12,
        scale: 2,
        signed: true,
        packed: false,
        length: 12,
      },
    },
    {
      name: 'acctCashCreditLimit',
      cobolName: 'ACCT-CASH-CREDIT-LIMIT',
      offset: 36,
      length: 12,
      filler: false,
      picture: {
        pic: 'S9(10)V99',
        kind: 'numeric',
        digits: 12,
        scale: 2,
        signed: true,
        packed: false,
        length: 12,
      },
    },
    {
      name: 'acctOpenDate',
      cobolName: 'ACCT-OPEN-DATE',
      offset: 48,
      length: 10,
      filler: false,
      picture: {
        pic: 'X(10)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 10,
      },
    },
    {
      name: 'acctExpiraionDate',
      cobolName: 'ACCT-EXPIRAION-DATE',
      offset: 58,
      length: 10,
      filler: false,
      picture: {
        pic: 'X(10)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 10,
      },
    },
    {
      name: 'acctReissueDate',
      cobolName: 'ACCT-REISSUE-DATE',
      offset: 68,
      length: 10,
      filler: false,
      picture: {
        pic: 'X(10)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 10,
      },
    },
    {
      name: 'acctCurrCycCredit',
      cobolName: 'ACCT-CURR-CYC-CREDIT',
      offset: 78,
      length: 12,
      filler: false,
      picture: {
        pic: 'S9(10)V99',
        kind: 'numeric',
        digits: 12,
        scale: 2,
        signed: true,
        packed: false,
        length: 12,
      },
    },
    {
      name: 'acctCurrCycDebit',
      cobolName: 'ACCT-CURR-CYC-DEBIT',
      offset: 90,
      length: 12,
      filler: false,
      picture: {
        pic: 'S9(10)V99',
        kind: 'numeric',
        digits: 12,
        scale: 2,
        signed: true,
        packed: false,
        length: 12,
      },
    },
    {
      name: 'acctAddrZip',
      cobolName: 'ACCT-ADDR-ZIP',
      offset: 102,
      length: 10,
      filler: false,
      picture: {
        pic: 'X(10)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 10,
      },
    },
    {
      name: 'acctGroupId',
      cobolName: 'ACCT-GROUP-ID',
      offset: 112,
      length: 10,
      filler: false,
      picture: {
        pic: 'X(10)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 10,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 122,
      length: 178,
      filler: true,
      picture: {
        pic: 'X(178)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 178,
      },
    },
  ],
};
