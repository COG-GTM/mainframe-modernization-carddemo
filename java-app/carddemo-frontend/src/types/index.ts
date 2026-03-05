// Types matching the Java DTOs and entities

export interface User {
  userId: string;
  firstName: string;
  lastName: string;
  userType: 'ADMIN' | 'USER';
  password?: string;
}

export interface Account {
  acctId: number;
  activeStatus: string;
  currentBalance: number;
  creditLimit: number;
  cashCreditLimit: number;
  openDate: string;
  expirationDate: string;
  reissueDate: string;
  currentCycleCredit: number;
  currentCycleDebit: number;
  groupId: string;
}

export interface Card {
  cardNum: string;
  acctId: number;
  activeStatus: string;
  embossedName: string;
}

export interface Customer {
  custId: number;
  firstName: string;
  middleName: string;
  lastName: string;
  addressLine1: string;
  addressLine2: string;
  addressLine3: string;
  city: string;
  state: string;
  zipCode: string;
  countryCode: string;
  phoneNumber1: string;
  phoneNumber2: string;
  ssn: string;
  govtIssuedId: string;
  dateOfBirth: string;
  efiAnnualIncome: number;
  creditScore: number;
}

export interface Transaction {
  transactionId: string;
  cardNum: string;
  transactionType: string;
  transactionCategoryCode: string;
  transactionSource: string;
  transactionDescription: string;
  transactionAmount: number;
  transactionTimestamp: string;
  merchantId: string;
  merchantName: string;
  merchantCity: string;
  merchantZip: string;
}

export interface TransactionType {
  typeCode: string;
  typeDescription: string;
}

export interface Authorization {
  authId: number;
  cardNum: string;
  authTimestamp: string;
  authType: string;
  transactionAmt: number;
  approvedAmt: number;
  merchantId: string;
  decision: string;
  declineReason: string;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  userId: string;
  userType: string;
  firstName: string;
  lastName: string;
}

export interface ReportRequest {
  startDate: string;
  endDate: string;
  acctId?: number;
}
