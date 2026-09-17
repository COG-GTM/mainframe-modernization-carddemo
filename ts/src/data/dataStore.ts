import { ACCOUNT_LAYOUT } from '../models/account';
import { CARD_LAYOUT } from '../models/card';
import { CARD_XREF_LAYOUT } from '../models/cardXref';
import { CUSTOMER_LAYOUT } from '../models/customer';
import { DISCLOSURE_GROUP_LAYOUT } from '../models/disclosureGroup';
import { TRAN_CATEGORY_LAYOUT } from '../models/tranCategory';
import { TRAN_CAT_BAL_LAYOUT } from '../models/tranCategoryBalance';
import { TRAN_TYPE_LAYOUT } from '../models/tranType';
import { DAILY_TRANSACTION_LAYOUT } from '../models/transaction';
import { AccountRepository } from './accountRepository';
import { CardRepository } from './cardRepository';
import { CardXrefRepository } from './cardXrefRepository';
import { CustomerRepository } from './customerRepository';
import { DailyTransactionRepository } from './dailyTransactionRepository';
import { DisclosureGroupRepository } from './disclosureGroupRepository';
import { readFixedWidthFile } from './fixedWidthFile';
import { SAMPLE_FILES } from './paths';
import { SEED_USERS } from './seedUsers';
import { TranCatBalRepository } from './tranCatBalRepository';
import { TranCategoryRepository } from './tranCategoryRepository';
import { TranTypeRepository } from './tranTypeRepository';
import { TransactionRepository } from './transactionRepository';
import { UserSecurityRepository } from './userSecurityRepository';

/**
 * The set of datasets the application runs against, i.e. the VSAM files that
 * CICS opens and the batch jobs read and write.
 */
export class DataStore {
  readonly users = new UserSecurityRepository();
  readonly accounts = new AccountRepository();
  readonly cards = new CardRepository();
  readonly customers = new CustomerRepository();
  readonly cardXref = new CardXrefRepository();
  readonly dailyTransactions = new DailyTransactionRepository();
  readonly transactions = new TransactionRepository();
  readonly disclosureGroups = new DisclosureGroupRepository();
  readonly tranCategories = new TranCategoryRepository();
  readonly tranTypes = new TranTypeRepository();
  readonly tranCatBalances = new TranCatBalRepository();

  /**
   * Loads the shipped sample data, equivalent to running DUSRSECJ, ACCTFILE,
   * CARDFILE, CUSTFILE, XREFFILE, DISCGRP, TCATBALF, TRANCATG and TRANTYPE.
   * TRANSACT starts empty, as TRANFILE loads only the initialisation record.
   */
  loadSampleData(): void {
    this.users.load(SEED_USERS);
    this.accounts.load(readFixedWidthFile(SAMPLE_FILES.acctdata, ACCOUNT_LAYOUT));
    this.cards.load(readFixedWidthFile(SAMPLE_FILES.carddata, CARD_LAYOUT));
    this.customers.load(readFixedWidthFile(SAMPLE_FILES.custdata, CUSTOMER_LAYOUT));
    this.cardXref.load(readFixedWidthFile(SAMPLE_FILES.cardxref, CARD_XREF_LAYOUT));
    this.dailyTransactions.load(
      readFixedWidthFile(SAMPLE_FILES.dailytran, DAILY_TRANSACTION_LAYOUT),
    );
    this.disclosureGroups.load(readFixedWidthFile(SAMPLE_FILES.discgrp, DISCLOSURE_GROUP_LAYOUT));
    this.tranCatBalances.load(readFixedWidthFile(SAMPLE_FILES.tcatbal, TRAN_CAT_BAL_LAYOUT));
    this.tranCategories.load(readFixedWidthFile(SAMPLE_FILES.trancatg, TRAN_CATEGORY_LAYOUT));
    this.tranTypes.load(readFixedWidthFile(SAMPLE_FILES.trantype, TRAN_TYPE_LAYOUT));
    this.transactions.load([]);
  }
}

let defaultStore: DataStore | undefined;

/** The process-wide store the online programs run against. */
export function getDataStore(): DataStore {
  if (defaultStore === undefined) {
    defaultStore = new DataStore();
    defaultStore.loadSampleData();
  }
  return defaultStore;
}

export function resetDataStore(store?: DataStore): void {
  defaultStore = store;
}
