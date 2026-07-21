export interface BillPaymentResponse {
  messageType: 'SUCCESS' | 'ERROR' | 'INFO';
  message: string;
  balance: number | null;
  balanceDisplay: string;
  accountId: number | null;
  transactionId: string | null;
  fieldInError: 'NONE' | 'ACCT_ID' | 'CONFIRM';
  cleared: boolean;
}
