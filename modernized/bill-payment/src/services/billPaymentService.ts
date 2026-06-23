import { Repositories } from '../repositories';
import { Account, isValidAcctId, normalizeAcctId } from '../models/account';
import {
  BILL_PAYMENT_TXN,
  Transaction,
  buildTimestamp,
  formatTransactionId,
} from '../models/transaction';
import { BillPaymentError, BILL_PAYMENT_MESSAGES } from '../errors';
import { BillPaymentRequest, BillPaymentResponse } from '../dto/billPayment';

/**
 * Bill Payment service — the modern equivalent of the COBIL00C
 * PROCESS-ENTER-KEY paragraph and its helper paragraphs
 * (READ-ACCTDAT-FILE, READ-CXACAIX-FILE, STARTBR/READPREV/WRITE-TRANSACT-FILE,
 * UPDATE-ACCTDAT-FILE).
 *
 * Business rule: a bill payment pays the account balance in full. It records a
 * transaction for the full current balance and then sets the balance to zero.
 */
export class BillPaymentService {
  constructor(
    private readonly repos: Repositories,
    private readonly clock: () => Date = () => new Date(),
  ) {}

  /**
   * Look up an account and return its current balance without paying anything.
   * Mirrors the COBIL00C path where CONFIRM is blank: READ-ACCTDAT-FILE only.
   */
  async getBalance(rawAcctId: string): Promise<BillPaymentResponse> {
    const account = await this.loadAccount(rawAcctId);
    return {
      paid: false,
      acctId: account.acctId,
      currentBalance: account.currentBalance.toString(),
      message: '',
    };
  }

  /**
   * Process the Enter key from the Bill Payment screen.
   *
   * @throws BillPaymentError for every validated failure path (empty/invalid
   *   account id, bad confirm value, account not found, nothing to pay, ...),
   *   matching the COBOL error paragraphs.
   */
  async processEnter(request: BillPaymentRequest): Promise<BillPaymentResponse> {
    // WHEN ACTIDINI = SPACES OR LOW-VALUES -> 'Acct ID can NOT be empty...'
    const rawAcctId = (request.acctId ?? '').trim();
    if (rawAcctId === '') {
      throw new BillPaymentError('ACCT_ID_REQUIRED');
    }

    // EVALUATE CONFIRMI: classify the confirm flag (case-insensitive).
    const confirm = (request.confirm ?? '').trim().toUpperCase();
    if (confirm !== '' && confirm !== 'Y' && confirm !== 'N') {
      // WHEN OTHER -> 'Invalid value. Valid values are (Y/N)...'
      throw new BillPaymentError('INVALID_CONFIRM');
    }
    if (confirm === 'N') {
      // WHEN 'N'/'n' -> CLEAR-CURRENT-SCREEN, set err flag (abort, nothing paid).
      return {
        paid: false,
        acctId: normalizeAcctId(rawAcctId),
        currentBalance: '',
        message: '',
      };
    }

    // READ-ACCTDAT-FILE (UPDATE intent — we will rewrite it if we pay).
    const account = await this.loadAccount(rawAcctId);

    // IF ACCT-CURR-BAL <= ZEROS -> 'You have nothing to pay...'
    if (account.currentBalance.isZeroOrLess()) {
      throw new BillPaymentError('NOTHING_TO_PAY');
    }

    // Blank confirm: display balance and prompt, do not pay.
    if (confirm !== 'Y') {
      return {
        paid: false,
        acctId: account.acctId,
        currentBalance: account.currentBalance.toString(),
        message: 'Confirm to make a bill payment...',
      };
    }

    // CONF-PAY-YES branch — execute the payment.
    return this.executePayment(account);
  }

  /** READ-ACCTDAT-FILE with NOTFND handling. */
  private async loadAccount(rawAcctId: string): Promise<Account> {
    const acctId = normalizeAcctId(rawAcctId);
    if (!isValidAcctId(acctId)) {
      throw new BillPaymentError('ACCT_ID_INVALID');
    }
    const account = await this.repos.accounts.findById(acctId);
    if (!account) {
      throw new BillPaymentError('ACCOUNT_NOT_FOUND');
    }
    return account;
  }

  private async executePayment(account: Account): Promise<BillPaymentResponse> {
    // READ-CXACAIX-FILE — resolve the card number for the account.
    const xref = await this.repos.cardXrefs.findByAcctId(account.acctId);
    if (!xref) {
      throw new BillPaymentError('XREF_NOT_FOUND');
    }

    // STARTBR(HIGH-VALUES) + READPREV + ENDBR -> highest tran id; ADD 1.
    const nextId = (await this.repos.transactions.maxTransactionId()) + 1;
    const amount = account.currentBalance; // pay balance in full

    const transaction: Transaction = {
      transactionId: formatTransactionId(nextId),
      typeCode: BILL_PAYMENT_TXN.TYPE_CODE,
      categoryCode: BILL_PAYMENT_TXN.CATEGORY_CODE,
      source: BILL_PAYMENT_TXN.SOURCE,
      description: BILL_PAYMENT_TXN.DESCRIPTION,
      amount,
      merchantId: BILL_PAYMENT_TXN.MERCHANT_ID,
      merchantName: BILL_PAYMENT_TXN.MERCHANT_NAME,
      merchantCity: BILL_PAYMENT_TXN.MERCHANT_CITY,
      merchantZip: BILL_PAYMENT_TXN.MERCHANT_ZIP,
      cardNumber: xref.cardNumber,
      originTimestamp: buildTimestamp(this.clock()),
      processTimestamp: buildTimestamp(this.clock()),
    };

    // WRITE-TRANSACT-FILE
    await this.repos.transactions.insert(transaction);

    // COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT ; UPDATE-ACCTDAT-FILE
    const updated: Account = {
      ...account,
      currentBalance: account.currentBalance.subtract(amount),
    };
    await this.repos.accounts.update(updated);

    return {
      paid: true,
      acctId: updated.acctId,
      currentBalance: updated.currentBalance.toString(),
      message: `Payment successful. Your Transaction ID is ${transaction.transactionId}.`,
      transactionId: transaction.transactionId,
    };
  }
}

export { BILL_PAYMENT_MESSAGES };
