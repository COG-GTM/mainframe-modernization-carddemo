import { alnum, digits, filler, type RecordLayout } from '../codec/fixedWidth.js';

/** COBOL copybook `app/cpy/CVCUS01Y.cpy` — CUSTOMER-RECORD (RECLN 500). */
export interface CustomerRecord {
  /** CUST-ID PIC 9(09) */
  custId: string;
  /** CUST-FIRST-NAME PIC X(25) */
  custFirstName: string;
  /** CUST-MIDDLE-NAME PIC X(25) */
  custMiddleName: string;
  /** CUST-LAST-NAME PIC X(25) */
  custLastName: string;
  /** CUST-ADDR-LINE-1 PIC X(50) */
  custAddrLine1: string;
  /** CUST-ADDR-LINE-2 PIC X(50) */
  custAddrLine2: string;
  /** CUST-ADDR-LINE-3 PIC X(50) */
  custAddrLine3: string;
  /** CUST-ADDR-STATE-CD PIC X(02) */
  custAddrStateCd: string;
  /** CUST-ADDR-COUNTRY-CD PIC X(03) */
  custAddrCountryCd: string;
  /** CUST-ADDR-ZIP PIC X(10) */
  custAddrZip: string;
  /** CUST-PHONE-NUM-1 PIC X(15) */
  custPhoneNum1: string;
  /** CUST-PHONE-NUM-2 PIC X(15) */
  custPhoneNum2: string;
  /** CUST-SSN PIC 9(09) */
  custSsn: string;
  /** CUST-GOVT-ISSUED-ID PIC X(20) */
  custGovtIssuedId: string;
  /** CUST-DOB-YYYY-MM-DD PIC X(10) */
  custDobYyyyMmDd: string;
  /** CUST-EFT-ACCOUNT-ID PIC X(10) */
  custEftAccountId: string;
  /** CUST-PRI-CARD-HOLDER-IND PIC X(01) */
  custPriCardHolderInd: string;
  /** CUST-FICO-CREDIT-SCORE PIC 9(03) */
  custFicoCreditScore: string;
}

export const CUSTOMER_LAYOUT: RecordLayout<CustomerRecord> = {
  copybook: 'CVCUS01Y',
  recordLength: 500,
  fields: [
    digits('custId', 9),
    alnum('custFirstName', 25),
    alnum('custMiddleName', 25),
    alnum('custLastName', 25),
    alnum('custAddrLine1', 50),
    alnum('custAddrLine2', 50),
    alnum('custAddrLine3', 50),
    alnum('custAddrStateCd', 2),
    alnum('custAddrCountryCd', 3),
    alnum('custAddrZip', 10),
    alnum('custPhoneNum1', 15),
    alnum('custPhoneNum2', 15),
    digits('custSsn', 9),
    alnum('custGovtIssuedId', 20),
    alnum('custDobYyyyMmDd', 10),
    alnum('custEftAccountId', 10),
    alnum('custPriCardHolderInd', 1),
    digits('custFicoCreditScore', 3),
    filler(168),
  ],
};
