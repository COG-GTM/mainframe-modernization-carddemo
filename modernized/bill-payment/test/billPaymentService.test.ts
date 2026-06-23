import { describe, it, expect, beforeEach } from 'vitest';
import { BillPaymentService } from '../src/services/billPaymentService';
import { BillPaymentError } from '../src/errors';
import { createSeededRepositories, Repositories } from '../src/repositories';

const FIXED_CLOCK = () => new Date('2026-06-23T22:00:00Z');

describe('BillPaymentService (COBIL00C equivalent)', () => {
  let repos: Repositories;
  let service: BillPaymentService;

  beforeEach(() => {
    repos = createSeededRepositories();
    service = new BillPaymentService(repos, FIXED_CLOCK);
  });

  it('pays the balance in full and zeroes the account (CONFIRM=Y)', async () => {
    const res = await service.processEnter({ acctId: '00000000011', confirm: 'Y' });

    expect(res.paid).toBe(true);
    expect(res.currentBalance).toBe('0.00');
    expect(res.transactionId).toBe('0000000000000101'); // max(100) + 1
    expect(res.message).toBe(
      'Payment successful. Your Transaction ID is 0000000000000101.',
    );

    const account = await repos.accounts.findById('00000000011');
    expect(account?.currentBalance.toString()).toBe('0.00');

    const txn = await repos.transactions.findById('0000000000000101');
    expect(txn?.amount.toString()).toBe('1234.56');
    expect(txn?.typeCode).toBe('02');
    expect(txn?.description).toBe('BILL PAYMENT - ONLINE');
    expect(txn?.cardNumber).toBe('4111111111111111');
    expect(txn?.originTimestamp).toBe('2026-06-23 22:00:00.000000');
  });

  it('is case-insensitive on the confirm flag', async () => {
    const res = await service.processEnter({ acctId: '00000000011', confirm: 'y' });
    expect(res.paid).toBe(true);
  });

  it('accepts an unpadded account id', async () => {
    const res = await service.processEnter({ acctId: '11', confirm: 'Y' });
    expect(res.paid).toBe(true);
    expect(res.acctId).toBe('00000000011');
  });

  it('only displays the balance when confirm is blank', async () => {
    const res = await service.processEnter({ acctId: '00000000011', confirm: '' });
    expect(res.paid).toBe(false);
    expect(res.currentBalance).toBe('1234.56');
    expect(res.message).toBe('Confirm to make a bill payment...');

    const account = await repos.accounts.findById('00000000011');
    expect(account?.currentBalance.toString()).toBe('1234.56'); // untouched
  });

  it('clears the screen and does not pay when confirm is N', async () => {
    const res = await service.processEnter({ acctId: '00000000011', confirm: 'N' });
    expect(res.paid).toBe(false);

    const account = await repos.accounts.findById('00000000011');
    expect(account?.currentBalance.toString()).toBe('1234.56');
  });

  it('rejects an empty account id', async () => {
    await expect(service.processEnter({ acctId: '', confirm: 'Y' })).rejects.toMatchObject({
      code: 'ACCT_ID_REQUIRED',
    });
  });

  it('rejects an invalid confirm value', async () => {
    await expect(
      service.processEnter({ acctId: '00000000011', confirm: 'X' }),
    ).rejects.toMatchObject({ code: 'INVALID_CONFIRM' });
  });

  it('rejects an unknown account', async () => {
    await expect(
      service.processEnter({ acctId: '99999999999', confirm: 'Y' }),
    ).rejects.toBeInstanceOf(BillPaymentError);
  });

  it('rejects when there is nothing to pay (balance <= 0)', async () => {
    await expect(
      service.processEnter({ acctId: '00000000022', confirm: 'Y' }),
    ).rejects.toMatchObject({ code: 'NOTHING_TO_PAY' });
  });

  it('getBalance returns the balance without paying', async () => {
    const res = await service.getBalance('00000000011');
    expect(res.paid).toBe(false);
    expect(res.currentBalance).toBe('1234.56');
  });
});
