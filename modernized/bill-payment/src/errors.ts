/**
 * Error types modeling the COBIL00C failure paths. Each COBOL error paragraph
 * set WS-MESSAGE and the WS-ERR-FLG before re-displaying the BMS map; here we
 * raise typed errors that the controller maps to HTTP status codes.
 */

export type BillPaymentErrorCode =
  | 'ACCT_ID_REQUIRED'
  | 'ACCT_ID_INVALID'
  | 'INVALID_CONFIRM'
  | 'ACCOUNT_NOT_FOUND'
  | 'NOTHING_TO_PAY'
  | 'XREF_NOT_FOUND'
  | 'TRAN_ID_DUPLICATE'
  | 'LOOKUP_FAILED'
  | 'UPDATE_FAILED';

/** Messages preserved verbatim from the COBOL WS-MESSAGE literals. */
export const BILL_PAYMENT_MESSAGES: Record<BillPaymentErrorCode, string> = {
  ACCT_ID_REQUIRED: 'Acct ID can NOT be empty...',
  ACCT_ID_INVALID: 'Account ID must be 11 digits...',
  INVALID_CONFIRM: 'Invalid value. Valid values are (Y/N)...',
  ACCOUNT_NOT_FOUND: 'Account ID NOT found...',
  NOTHING_TO_PAY: 'You have nothing to pay...',
  XREF_NOT_FOUND: 'Account ID NOT found...',
  TRAN_ID_DUPLICATE: 'Tran ID already exist...',
  LOOKUP_FAILED: 'Unable to lookup Account...',
  UPDATE_FAILED: 'Unable to Update Account...',
};

export class BillPaymentError extends Error {
  constructor(public readonly code: BillPaymentErrorCode, message?: string) {
    super(message ?? BILL_PAYMENT_MESSAGES[code]);
    this.name = 'BillPaymentError';
  }
}
