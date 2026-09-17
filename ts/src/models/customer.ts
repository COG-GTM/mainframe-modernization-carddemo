/**
 * Generated from app/cpy/CVCUS01Y.cpy by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run `npm run generate:models` instead.
 *
 * Record length: 500 bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface CustomerRecord {
  /** CUST-ID PIC 9(09), bytes 1-9 */
  custId: number;
  /** CUST-FIRST-NAME PIC X(25), bytes 10-34 */
  custFirstName: string;
  /** CUST-MIDDLE-NAME PIC X(25), bytes 35-59 */
  custMiddleName: string;
  /** CUST-LAST-NAME PIC X(25), bytes 60-84 */
  custLastName: string;
  /** CUST-ADDR-LINE-1 PIC X(50), bytes 85-134 */
  custAddrLine1: string;
  /** CUST-ADDR-LINE-2 PIC X(50), bytes 135-184 */
  custAddrLine2: string;
  /** CUST-ADDR-LINE-3 PIC X(50), bytes 185-234 */
  custAddrLine3: string;
  /** CUST-ADDR-STATE-CD PIC X(02), bytes 235-236 */
  custAddrStateCd: string;
  /** CUST-ADDR-COUNTRY-CD PIC X(03), bytes 237-239 */
  custAddrCountryCd: string;
  /** CUST-ADDR-ZIP PIC X(10), bytes 240-249 */
  custAddrZip: string;
  /** CUST-PHONE-NUM-1 PIC X(15), bytes 250-264 */
  custPhoneNum1: string;
  /** CUST-PHONE-NUM-2 PIC X(15), bytes 265-279 */
  custPhoneNum2: string;
  /** CUST-SSN PIC 9(09), bytes 280-288 */
  custSsn: number;
  /** CUST-GOVT-ISSUED-ID PIC X(20), bytes 289-308 */
  custGovtIssuedId: string;
  /** CUST-DOB-YYYY-MM-DD PIC X(10), bytes 309-318 */
  custDobYyyyMmDd: string;
  /** CUST-EFT-ACCOUNT-ID PIC X(10), bytes 319-328 */
  custEftAccountId: string;
  /** CUST-PRI-CARD-HOLDER-IND PIC X(01), bytes 329-329 */
  custPriCardHolderInd: string;
  /** CUST-FICO-CREDIT-SCORE PIC 9(03), bytes 330-332 */
  custFicoCreditScore: number;
}

export const CUSTOMER_RECORD_LENGTH = 500;

export const CUSTOMER_RECORD_SPEC: RecordSpec<CustomerRecord> = {
  name: 'CustomerRecord',
  copybook: 'app/cpy/CVCUS01Y.cpy',
  recordLength: 500,
  fields: [
    {
      name: 'custId',
      cobolName: 'CUST-ID',
      offset: 0,
      length: 9,
      filler: false,
      picture: {
        pic: '9(09)',
        kind: 'numeric',
        digits: 9,
        scale: 0,
        signed: false,
        packed: false,
        length: 9,
      },
    },
    {
      name: 'custFirstName',
      cobolName: 'CUST-FIRST-NAME',
      offset: 9,
      length: 25,
      filler: false,
      picture: {
        pic: 'X(25)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 25,
      },
    },
    {
      name: 'custMiddleName',
      cobolName: 'CUST-MIDDLE-NAME',
      offset: 34,
      length: 25,
      filler: false,
      picture: {
        pic: 'X(25)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 25,
      },
    },
    {
      name: 'custLastName',
      cobolName: 'CUST-LAST-NAME',
      offset: 59,
      length: 25,
      filler: false,
      picture: {
        pic: 'X(25)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 25,
      },
    },
    {
      name: 'custAddrLine1',
      cobolName: 'CUST-ADDR-LINE-1',
      offset: 84,
      length: 50,
      filler: false,
      picture: {
        pic: 'X(50)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 50,
      },
    },
    {
      name: 'custAddrLine2',
      cobolName: 'CUST-ADDR-LINE-2',
      offset: 134,
      length: 50,
      filler: false,
      picture: {
        pic: 'X(50)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 50,
      },
    },
    {
      name: 'custAddrLine3',
      cobolName: 'CUST-ADDR-LINE-3',
      offset: 184,
      length: 50,
      filler: false,
      picture: {
        pic: 'X(50)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 50,
      },
    },
    {
      name: 'custAddrStateCd',
      cobolName: 'CUST-ADDR-STATE-CD',
      offset: 234,
      length: 2,
      filler: false,
      picture: {
        pic: 'X(02)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 2,
      },
    },
    {
      name: 'custAddrCountryCd',
      cobolName: 'CUST-ADDR-COUNTRY-CD',
      offset: 236,
      length: 3,
      filler: false,
      picture: {
        pic: 'X(03)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 3,
      },
    },
    {
      name: 'custAddrZip',
      cobolName: 'CUST-ADDR-ZIP',
      offset: 239,
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
      name: 'custPhoneNum1',
      cobolName: 'CUST-PHONE-NUM-1',
      offset: 249,
      length: 15,
      filler: false,
      picture: {
        pic: 'X(15)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 15,
      },
    },
    {
      name: 'custPhoneNum2',
      cobolName: 'CUST-PHONE-NUM-2',
      offset: 264,
      length: 15,
      filler: false,
      picture: {
        pic: 'X(15)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 15,
      },
    },
    {
      name: 'custSsn',
      cobolName: 'CUST-SSN',
      offset: 279,
      length: 9,
      filler: false,
      picture: {
        pic: '9(09)',
        kind: 'numeric',
        digits: 9,
        scale: 0,
        signed: false,
        packed: false,
        length: 9,
      },
    },
    {
      name: 'custGovtIssuedId',
      cobolName: 'CUST-GOVT-ISSUED-ID',
      offset: 288,
      length: 20,
      filler: false,
      picture: {
        pic: 'X(20)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 20,
      },
    },
    {
      name: 'custDobYyyyMmDd',
      cobolName: 'CUST-DOB-YYYY-MM-DD',
      offset: 308,
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
      name: 'custEftAccountId',
      cobolName: 'CUST-EFT-ACCOUNT-ID',
      offset: 318,
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
      name: 'custPriCardHolderInd',
      cobolName: 'CUST-PRI-CARD-HOLDER-IND',
      offset: 328,
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
      name: 'custFicoCreditScore',
      cobolName: 'CUST-FICO-CREDIT-SCORE',
      offset: 329,
      length: 3,
      filler: false,
      picture: {
        pic: '9(03)',
        kind: 'numeric',
        digits: 3,
        scale: 0,
        signed: false,
        packed: false,
        length: 3,
      },
    },
    {
      name: 'filler1',
      cobolName: 'FILLER',
      offset: 332,
      length: 168,
      filler: true,
      picture: {
        pic: 'X(168)',
        kind: 'alphanumeric',
        digits: 0,
        scale: 0,
        signed: false,
        packed: false,
        length: 168,
      },
    },
  ],
};
