import { Decimal } from 'decimal.js';
import { describe, expect, it } from 'vitest';
import { createFileBackedRepositories } from '../../src/data/fileRepositories.js';
import { ACCOUNT_VIEW_MESSAGES, viewAccount } from '../../src/online/accountViewService.js';
import { ACCOUNT, CUSTOMER, XREF, signedOnCommarea, testRepositories } from './fixtures.js';

/** Parity reference: `app/cbl/COACTVWC.cbl` (2210-EDIT-ACCOUNT, 9200/9300/9400). */
describe('COACTVWC account view', () => {
  const commarea = signedOnCommarea('U');

  it.each(['', '   ', '*'])('treats %j as no search criteria', (rawAcctId) => {
    const result = viewAccount(rawAcctId, commarea, testRepositories());

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe('Account number not provided');
    expect(result.commarea.acctId).toBe('');
  });

  it.each(['1234567890A', 'abcdefghijk', '0000000000-'])(
    'rejects the non-numeric filter %j',
    (rawAcctId) => {
      const result = viewAccount(rawAcctId, commarea, testRepositories());

      expect(result.ok).toBe(false);
      if (result.ok) return;
      expect(result.errorMessage).toBe('Account Filter must  be a non-zero 11 digit number');
    },
  );

  it('rejects a zero filter (CC-ACCT-ID EQUAL ZEROES)', () => {
    const result = viewAccount('00000000000', commarea, testRepositories());

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe(ACCOUNT_VIEW_MESSAGES.acctFilterInvalid);
    expect(result.commarea.acctId).toBe('');
  });

  it('formats the CXACAIX not-found message the way 9200 STRINGs it', () => {
    const result = viewAccount('00000000099', commarea, testRepositories());

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe(
      'Account:00000000099 not found in Cross ref file.  Resp:000000013  Reas:000000000',
    );
    expect(result.commarea.acctId).toBe('00000000099');
  });

  it('formats the ACCTDAT not-found message the way 9300 STRINGs it', () => {
    const repositories = testRepositories({ accounts: [] });

    const result = viewAccount(ACCOUNT.acctId, commarea, repositories);

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe(
      'Account:00000000011 not found in Acct Master file.Resp:000000013  Reas:000000000',
    );
  });

  it('formats the CUSTDAT not-found message the way 9400 STRINGs it', () => {
    const repositories = testRepositories({ customers: [] });

    const result = viewAccount(ACCOUNT.acctId, commarea, repositories);

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe(
      'CustId:000000011 not found in customer master.Resp: 000000013  REAS:000000000',
    );
    expect(result.commarea).toMatchObject({ acctId: ACCOUNT.acctId, custId: CUSTOMER.custId });
  });

  it('returns account, customer and xref and updates the COMMAREA on the happy path', () => {
    const result = viewAccount(ACCOUNT.acctId, commarea, testRepositories());

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.account).toEqual(ACCOUNT);
    expect(result.customer).toEqual(CUSTOMER);
    expect(result.xref).toEqual(XREF);
    expect(result.infoMessage).toBe('Displaying details of given Account');
    expect(result.account.acctCurrBal).toBeInstanceOf(Decimal);
    expect(result.account.acctCurrBal.toFixed(2)).toBe('1234.56');
    expect(result.commarea).toMatchObject({
      acctId: ACCOUNT.acctId,
      acctStatus: 'Y',
      custId: CUSTOMER.custId,
      custFname: CUSTOMER.custFirstName,
      custMname: CUSTOMER.custMiddleName,
      custLname: CUSTOMER.custLastName,
      cardNum: XREF.xrefCardNum,
      userId: commarea.userId,
    });
  });

  it('left-pads a short filter to the PIC 9(11) key', () => {
    const result = viewAccount('11', commarea, testRepositories());

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.account.acctId).toBe('00000000011');
  });

  it('reads a real account out of app/data/ASCII through the file-backed repositories', () => {
    const repositories = createFileBackedRepositories();

    const result = viewAccount('00000000001', commarea, repositories);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.account.acctId).toBe('00000000001');
    expect(result.account.acctActiveStatus).toBe('Y');
    expect(result.account.acctCurrBal).toBeInstanceOf(Decimal);
    expect(result.account.acctCurrBal.toFixed(2)).toBe('194.00');
    expect(result.account.acctCreditLimit.toFixed(2)).toBe('2020.00');
    expect(result.xref.xrefAcctId).toBe('00000000001');
    expect(result.customer.custId).toBe(result.xref.xrefCustId);
  });
});
