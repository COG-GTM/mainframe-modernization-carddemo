import type { CardDemoRepositories } from '../data/repositories.js';
import type { AccountRecord } from '../domain/account.js';
import type { CardXrefRecord } from '../domain/cardXref.js';
import type { CardDemoCommarea } from '../domain/commarea.js';
import type { CustomerRecord } from '../domain/customer.js';

/** WS-INFO-MSG / WS-RETURN-MSG 88-levels in `app/cbl/COACTVWC.cbl`. */
export const ACCOUNT_VIEW_MESSAGES = {
  promptForInput: 'Enter or update id of account to display',
  informOutput: 'Displaying details of given Account',
  promptForAcct: 'Account number not provided',
  acctFilterInvalid: 'Account Filter must  be a non-zero 11 digit number',
} as const;

/** CICS RESP values the COBOL formats into its not-found messages. */
const RESP_NOTFND = 13;
const RESP2_NOTFND = 0;

function respText(value: number): string {
  return String(value).padStart(9, '0');
}

function notFoundMessage(prefix: string, key: string, suffix: string): string {
  return `${prefix}${key} not found in${suffix}Resp:${respText(RESP_NOTFND)} Reas:${respText(
    RESP2_NOTFND,
  )}`;
}

export interface AccountViewResult {
  account: AccountRecord;
  customer: CustomerRecord;
  xref: CardXrefRecord;
  infoMessage: string;
  commarea: CardDemoCommarea;
}

export type AccountViewResponse =
  | ({ ok: true } & AccountViewResult)
  | { ok: false; errorMessage: string; commarea: CardDemoCommarea };

/**
 * `COACTVWC` — 2210-EDIT-ACCOUNT, 9200-GETCARDXREF-BYACCT,
 * 9300-GETACCTDATA-BYACCT and 9400-GETCUSTDATA-BYCUST.
 *
 * An account id of `*` or spaces is the BMS "no filter" value.
 */
export function viewAccount(
  rawAcctId: string,
  commarea: CardDemoCommarea,
  repositories: CardDemoRepositories,
): AccountViewResponse {
  const filter = rawAcctId.trim() === '*' ? '' : rawAcctId.trim();

  if (filter === '') {
    return {
      ok: false,
      errorMessage: ACCOUNT_VIEW_MESSAGES.promptForAcct,
      commarea: { ...commarea, acctId: '' },
    };
  }

  if (!/^\d+$/.test(filter) || Number(filter) === 0) {
    return {
      ok: false,
      errorMessage: ACCOUNT_VIEW_MESSAGES.acctFilterInvalid,
      commarea: { ...commarea, acctId: '' },
    };
  }

  const acctId = filter.padStart(11, '0');

  const xref = repositories.cardXrefsByAccount.read(acctId);
  if (xref === undefined) {
    return {
      ok: false,
      errorMessage: notFoundMessage('Account:', acctId, ' Cross ref file.  '),
      commarea: { ...commarea, acctId },
    };
  }

  const account = repositories.accounts.read(acctId);
  if (account === undefined) {
    return {
      ok: false,
      errorMessage: notFoundMessage('Account:', acctId, ' Acct Master file.'),
      commarea: { ...commarea, acctId },
    };
  }

  const customer = repositories.customers.read(xref.xrefCustId);
  if (customer === undefined) {
    return {
      ok: false,
      errorMessage: notFoundMessage('CustId:', xref.xrefCustId, ' customer master.'),
      commarea: { ...commarea, acctId, custId: xref.xrefCustId },
    };
  }

  return {
    ok: true,
    account,
    customer,
    xref,
    infoMessage: ACCOUNT_VIEW_MESSAGES.informOutput,
    commarea: {
      ...commarea,
      acctId,
      acctStatus: account.acctActiveStatus,
      custId: xref.xrefCustId,
      custFname: customer.custFirstName,
      custMname: customer.custMiddleName,
      custLname: customer.custLastName,
      cardNum: xref.xrefCardNum,
    },
  };
}
