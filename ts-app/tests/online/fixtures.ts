import { Decimal } from 'decimal.js';
import { InMemoryKeyedRepository, type CardDemoRepositories } from '../../src/data/repositories.js';
import type {
  AccountRecord,
  CardRecord,
  CardXrefRecord,
  CustomerRecord,
  SecUserRecord,
  TranCatBalanceRecord,
  TransactionRecord,
} from '../../src/domain/index.js';
import { emptyCommarea, type CardDemoCommarea } from '../../src/domain/commarea.js';
import { PROGRAMS, TRANSACTION_IDS } from '../../src/online/routes.js';

export const ADMIN_USER: SecUserRecord = {
  secUsrId: 'ADMIN001',
  secUsrFname: 'MARGARET',
  secUsrLname: 'GOLD',
  secUsrPwd: 'PASSWORD',
  secUsrType: 'A',
};

export const REGULAR_USER: SecUserRecord = {
  secUsrId: 'USER0001',
  secUsrFname: 'LAWRENCE',
  secUsrLname: 'THOMAS',
  secUsrPwd: 'PASSWORD',
  secUsrType: 'U',
};

export function userRepository(
  users: readonly SecUserRecord[] = [ADMIN_USER, REGULAR_USER],
): InMemoryKeyedRepository<SecUserRecord> {
  return new InMemoryKeyedRepository<SecUserRecord>((record) => record.secUsrId, users);
}

export const ACCOUNT: AccountRecord = {
  acctId: '00000000011',
  acctActiveStatus: 'Y',
  acctCurrBal: new Decimal('1234.56'),
  acctCreditLimit: new Decimal('5000.00'),
  acctCashCreditLimit: new Decimal('500.00'),
  acctOpenDate: '2014-11-20',
  acctExpiraionDate: '2025-05-20',
  acctReissueDate: '2025-05-20',
  acctCurrCycCredit: new Decimal('0.00'),
  acctCurrCycDebit: new Decimal('-25.75'),
  acctAddrZip: '12345',
  acctGroupId: 'A000000000',
};

export const CUSTOMER: CustomerRecord = {
  custId: '000000011',
  custFirstName: 'ARTHUR',
  custMiddleName: 'B',
  custLastName: 'CRANE',
  custAddrLine1: '1 Main Street',
  custAddrLine2: 'Apt 2',
  custAddrLine3: 'Springfield',
  custAddrStateCd: 'NY',
  custAddrCountryCd: 'USA',
  custAddrZip: '12345',
  custPhoneNum1: '(212)1234567',
  custPhoneNum2: '(212)7654321',
  custSsn: '123456789',
  custGovtIssuedId: 'NY1234567',
  custDobYyyyMmDd: '1970-01-01',
  custEftAccountId: '1234567890',
  custPriCardHolderInd: 'Y',
  custFicoCreditScore: '750',
};

export const XREF: CardXrefRecord = {
  xrefCardNum: '4111111111111111',
  xrefCustId: CUSTOMER.custId,
  xrefAcctId: ACCOUNT.acctId,
};

export interface FixtureOverrides {
  accounts?: readonly AccountRecord[];
  customers?: readonly CustomerRecord[];
  xrefs?: readonly CardXrefRecord[];
  users?: readonly SecUserRecord[];
}

/** A minimal in-memory stand-in for the CICS files COACTVWC reads. */
export function testRepositories(overrides: FixtureOverrides = {}): CardDemoRepositories {
  const accounts = overrides.accounts ?? [ACCOUNT];
  const customers = overrides.customers ?? [CUSTOMER];
  const xrefs = overrides.xrefs ?? [XREF];

  return {
    users: userRepository(overrides.users),
    accounts: new InMemoryKeyedRepository<AccountRecord>((record) => record.acctId, accounts),
    cards: new InMemoryKeyedRepository<CardRecord>((record) => record.cardNum),
    cardXrefs: new InMemoryKeyedRepository<CardXrefRecord>((record) => record.xrefCardNum, xrefs),
    cardXrefsByAccount: new InMemoryKeyedRepository<CardXrefRecord>(
      (record) => record.xrefAcctId,
      xrefs,
    ),
    customers: new InMemoryKeyedRepository<CustomerRecord>((record) => record.custId, customers),
    tranCatBalances: new InMemoryKeyedRepository<TranCatBalanceRecord>(
      (record) => record.trancatAcctId,
    ),
    transactions: new InMemoryKeyedRepository<TransactionRecord>((record) => record.tranId),
    dailyTransactions: [],
  };
}

/** The COMMAREA COMEN01C / COACTVWC receive after a successful signon. */
export function signedOnCommarea(userType: 'A' | 'U' = 'U'): CardDemoCommarea {
  return {
    ...emptyCommarea(),
    fromTranid: TRANSACTION_IDS.COSGN00C,
    fromProgram: PROGRAMS.signon,
    toProgram: userType === 'A' ? PROGRAMS.adminMenu : PROGRAMS.mainMenu,
    userId: userType === 'A' ? ADMIN_USER.secUsrId : REGULAR_USER.secUsrId,
    userType,
  };
}
