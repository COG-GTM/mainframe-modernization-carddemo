import { defineLayout } from '../copybooks/layout.js';

/**
 * CBTRN02C WORKING-STORAGE REJECT-RECORD (DALYREJS, RECFM=F LRECL=430):
 *   REJECT-TRAN-DATA   PIC X(350)  – the DALYTRAN-RECORD as read
 *   VALIDATION-TRAILER PIC X(80)   – WS-VALIDATION-TRAILER:
 *     WS-VALIDATION-FAIL-REASON      PIC 9(04)
 *     WS-VALIDATION-FAIL-REASON-DESC PIC X(76)
 */
export interface RejectRecord {
  rejectTranData: string;
  validationFailReason: string;
  validationFailReasonDesc: string;
}

export const REJECT_RECORD_LAYOUT = defineLayout<RejectRecord>('REJECT-RECORD (CBTRN02C)', [
  { kind: 'alpha', name: 'rejectTranData', length: 350 },
  { kind: 'unsigned', name: 'validationFailReason', length: 4 },
  { kind: 'alpha', name: 'validationFailReasonDesc', length: 76 },
]);

/** WS-VALIDATION-FAIL-REASON values set by CBTRN02C. */
export const RejectReason = {
  /** 1500-A-LOOKUP-XREF: card number not on CARDXREF */
  INVALID_CARD_NUMBER: 100,
  /** 1500-B-LOOKUP-ACCT: XREF-ACCT-ID not on ACCTDATA */
  ACCOUNT_NOT_FOUND: 101,
  /** 1500-B-LOOKUP-ACCT: cycle credit - cycle debit + amount > credit limit */
  OVERLIMIT: 102,
  /** 1500-B-LOOKUP-ACCT: ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10) */
  ACCOUNT_EXPIRED: 103,
  /** 2800-UPDATE-ACCOUNT-REC: REWRITE INVALID KEY (set after posting decision) */
  ACCOUNT_REWRITE_NOT_FOUND: 109,
} as const;

export type RejectReasonCode = (typeof RejectReason)[keyof typeof RejectReason];

export const RejectReasonDesc: Record<RejectReasonCode, string> = {
  100: 'INVALID CARD NUMBER FOUND',
  101: 'ACCOUNT RECORD NOT FOUND',
  102: 'OVERLIMIT TRANSACTION',
  103: 'TRANSACTION RECEIVED AFTER ACCT EXPIRATION',
  109: 'ACCOUNT RECORD NOT FOUND',
};
