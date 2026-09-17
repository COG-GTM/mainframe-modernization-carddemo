import { RecordLayout } from './layout';

/**
 * CUSTOMER-RECORD (copybook CVCUS01Y, LRECL 500, dataset CUSTDATA).
 * CUSTREC.cpy declares the same layout with CUST-DOB-YYYYMMDD spelled without
 * hyphens; both map to `custDobYyyyMmDd` here.
 *
 * off  len  pic        field
 *   0    9  9(09)      CUST-ID
 *   9   25  X(25)      CUST-FIRST-NAME
 *  34   25  X(25)      CUST-MIDDLE-NAME
 *  59   25  X(25)      CUST-LAST-NAME
 *  84   50  X(50)      CUST-ADDR-LINE-1
 * 134   50  X(50)      CUST-ADDR-LINE-2
 * 184   50  X(50)      CUST-ADDR-LINE-3
 * 234    2  X(02)      CUST-ADDR-STATE-CD
 * 236    3  X(03)      CUST-ADDR-COUNTRY-CD
 * 239   10  X(10)      CUST-ADDR-ZIP
 * 249   15  X(15)      CUST-PHONE-NUM-1
 * 264   15  X(15)      CUST-PHONE-NUM-2
 * 279    9  9(09)      CUST-SSN
 * 288   20  X(20)      CUST-GOVT-ISSUED-ID
 * 308   10  X(10)      CUST-DOB-YYYY-MM-DD
 * 318   10  X(10)      CUST-EFT-ACCOUNT-ID
 * 328    1  X(01)      CUST-PRI-CARD-HOLDER-IND
 * 329    3  9(03)      CUST-FICO-CREDIT-SCORE
 * 332  168  X(168)     FILLER
 */
export interface CustomerRecord {
  custId: number;
  custFirstName: string;
  custMiddleName: string;
  custLastName: string;
  custAddrLine1: string;
  custAddrLine2: string;
  custAddrLine3: string;
  custAddrStateCd: string;
  custAddrCountryCd: string;
  custAddrZip: string;
  custPhoneNum1: string;
  custPhoneNum2: string;
  custSsn: number;
  custGovtIssuedId: string;
  custDobYyyyMmDd: string;
  custEftAccountId: string;
  custPriCardHolderInd: string;
  custFicoCreditScore: number;
}

export const CUSTOMER_RECORD_LENGTH = 500;

export const CUSTOMER_LAYOUT: RecordLayout<CustomerRecord> = {
  copybook: 'CVCUS01Y',
  length: CUSTOMER_RECORD_LENGTH,
  fields: [
    { kind: 'unsigned', name: 'custId', length: 9 },
    { kind: 'alphanumeric', name: 'custFirstName', length: 25 },
    { kind: 'alphanumeric', name: 'custMiddleName', length: 25 },
    { kind: 'alphanumeric', name: 'custLastName', length: 25 },
    { kind: 'alphanumeric', name: 'custAddrLine1', length: 50 },
    { kind: 'alphanumeric', name: 'custAddrLine2', length: 50 },
    { kind: 'alphanumeric', name: 'custAddrLine3', length: 50 },
    { kind: 'alphanumeric', name: 'custAddrStateCd', length: 2 },
    { kind: 'alphanumeric', name: 'custAddrCountryCd', length: 3 },
    { kind: 'alphanumeric', name: 'custAddrZip', length: 10 },
    { kind: 'alphanumeric', name: 'custPhoneNum1', length: 15 },
    { kind: 'alphanumeric', name: 'custPhoneNum2', length: 15 },
    { kind: 'unsigned', name: 'custSsn', length: 9 },
    { kind: 'alphanumeric', name: 'custGovtIssuedId', length: 20 },
    { kind: 'alphanumeric', name: 'custDobYyyyMmDd', length: 10 },
    { kind: 'alphanumeric', name: 'custEftAccountId', length: 10 },
    { kind: 'alphanumeric', name: 'custPriCardHolderInd', length: 1 },
    { kind: 'unsigned', name: 'custFicoCreditScore', length: 3 },
    { kind: 'filler', length: 168 },
  ],
  fromFields: (v) => ({
    custId: v.custId as number,
    custFirstName: v.custFirstName as string,
    custMiddleName: v.custMiddleName as string,
    custLastName: v.custLastName as string,
    custAddrLine1: v.custAddrLine1 as string,
    custAddrLine2: v.custAddrLine2 as string,
    custAddrLine3: v.custAddrLine3 as string,
    custAddrStateCd: v.custAddrStateCd as string,
    custAddrCountryCd: v.custAddrCountryCd as string,
    custAddrZip: v.custAddrZip as string,
    custPhoneNum1: v.custPhoneNum1 as string,
    custPhoneNum2: v.custPhoneNum2 as string,
    custSsn: v.custSsn as number,
    custGovtIssuedId: v.custGovtIssuedId as string,
    custDobYyyyMmDd: v.custDobYyyyMmDd as string,
    custEftAccountId: v.custEftAccountId as string,
    custPriCardHolderInd: v.custPriCardHolderInd as string,
    custFicoCreditScore: v.custFicoCreditScore as number,
  }),
  toFields: (r) => ({ ...r }),
};
