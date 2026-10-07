import { describe, expect, it } from 'vitest';
import { AbendError, Cbtrn02c, db2FormatTimestamp, formatIoStatus, RejectReason, runCbtrn02c } from '../src/cbtrn02c/index.js';
import { dec } from '../src/copybooks/numeric.js';
import { TRAN_CAT_BAL_LAYOUT } from '../src/copybooks/index.js';
import { ACCT, account, dailyTran, FIXED_CLOCK, harness, xref } from './helpers.js';

const run = (h: ReturnType<typeof harness>, display: string[] = []) =>
  runCbtrn02c(h.files, { clock: FIXED_CLOCK, display: (l) => display.push(l) });

describe('1500-VALIDATE-TRAN reject paths', () => {
  it('100 – card number not on XREF; account lookup is skipped', async () => {
    const h = harness({ trans: [dailyTran({ dalytranCardNum: '9999999999999999' })], accounts: [] });
    const result = await run(h);
    expect(result).toEqual({ returnCode: 4, transactionCount: 1, rejectCount: 1 });
    expect(h.rejects.records).toHaveLength(1);
    expect(h.rejects.records[0]?.validationFailReason).toBe('0100');
    expect(h.rejects.records[0]?.validationFailReasonDesc).toBe('INVALID CARD NUMBER FOUND');
    expect(h.transactions.entries()).toHaveLength(0);
  });

  it('101 – xref points at a missing account', async () => {
    const h = harness({ trans: [dailyTran()], xrefs: [xref(undefined, '00000000099')] });
    await run(h);
    expect(h.rejects.records[0]?.validationFailReason).toBe('0101');
    expect(h.rejects.records[0]?.validationFailReasonDesc).toBe('ACCOUNT RECORD NOT FOUND');
  });

  it('102 – cycle credit - cycle debit + amount exceeds credit limit', async () => {
    const h = harness({
      trans: [dailyTran({ dalytranAmt: dec('600.01') })],
      accounts: [account({ acctCurrCycCredit: dec('500.00'), acctCurrCycDebit: dec('100.00') })],
    });
    await run(h);
    expect(h.rejects.records[0]?.validationFailReason).toBe('0102');
    expect(h.rejects.records[0]?.validationFailReasonDesc).toBe('OVERLIMIT TRANSACTION');
    // rejected transactions do not touch the account
    expect(h.accounts.entries()[0]?.acctCurrBal.toFixed(2)).toBe('500.00');
  });

  it('102 boundary – exactly at the credit limit is accepted', async () => {
    const h = harness({
      trans: [dailyTran({ dalytranAmt: dec('600.00') })],
      accounts: [account({ acctCurrCycCredit: dec('500.00'), acctCurrCycDebit: dec('100.00') })],
    });
    const result = await run(h);
    expect(result.rejectCount).toBe(0);
  });

  it('103 – transaction dated after account expiration', async () => {
    const h = harness({
      trans: [dailyTran({ dalytranOrigTs: '2025-05-21 00:00:00.000000' })],
      accounts: [account({ acctExpirationDate: '2025-05-20' })],
    });
    await run(h);
    expect(h.rejects.records[0]?.validationFailReason).toBe('0103');
    expect(h.rejects.records[0]?.validationFailReasonDesc).toBe('TRANSACTION RECEIVED AFTER ACCT EXPIRATION');
  });

  it('103 boundary – transaction on the expiration date is accepted', async () => {
    const h = harness({
      trans: [dailyTran({ dalytranOrigTs: '2025-05-20 23:59:59.000000' })],
      accounts: [account({ acctExpirationDate: '2025-05-20' })],
    });
    expect((await run(h)).rejectCount).toBe(0);
  });

  it('overlimit AND expired – 103 overwrites 102 (COBOL check order)', async () => {
    const h = harness({
      trans: [dailyTran({ dalytranAmt: dec('5000.00'), dalytranOrigTs: '2026-01-01 00:00:00.000000' })],
    });
    await run(h);
    expect(h.rejects.records[0]?.validationFailReason).toBe('0103');
  });

  it('reject record carries the full 350-byte DALYTRAN record + 80-byte trailer', async () => {
    const h = harness({ trans: [dailyTran({ dalytranCardNum: '0000000000000000' })] });
    await run(h);
    const rej = h.rejects.records[0];
    expect(rej?.rejectTranData.startsWith('0000000000000001010001POS TERM')).toBe(true);
  });

  it('RC is 0 when nothing is rejected', async () => {
    const result = await run(harness({ trans: [dailyTran()] }));
    expect(result).toEqual({ returnCode: 0, transactionCount: 1, rejectCount: 0 });
  });
});

describe('2000-POST-TRANSACTION', () => {
  it('copies DALYTRAN fields to TRAN-RECORD and stamps TRAN-PROC-TS', async () => {
    const h = harness({ trans: [dailyTran()] });
    await run(h);
    const [tran] = h.transactions.entries();
    expect(tran?.tranId).toBe('0000000000000001');
    expect(tran?.tranAmt.toFixed(2)).toBe('100.00');
    expect(tran?.tranCardNum).toBe('4859452612877065');
    expect(tran?.tranOrigTs).toBe('2022-06-10 19:27:53.000000');
    expect(tran?.tranProcTs).toBe('2022-06-11-00.00.00.340000');
  });

  it('2800 – credits go to CYC-CREDIT, debits to CYC-DEBIT, both to CURR-BAL', async () => {
    const h = harness({
      trans: [
        dailyTran({ dalytranId: 'A000000000000001', dalytranAmt: dec('100.25') }),
        dailyTran({ dalytranId: 'A000000000000002', dalytranAmt: dec('-40.10') }),
      ],
    });
    await run(h);
    const acct = h.accounts.entries()[0];
    expect(acct?.acctCurrBal.toFixed(2)).toBe('560.15');
    expect(acct?.acctCurrCycCredit.toFixed(2)).toBe('100.25');
    expect(acct?.acctCurrCycDebit.toFixed(2)).toBe('-40.10');
  });

  it('later transactions are validated against the updated account', async () => {
    const h = harness({
      trans: [
        dailyTran({ dalytranId: 'A000000000000001', dalytranAmt: dec('900.00') }),
        dailyTran({ dalytranId: 'A000000000000002', dalytranAmt: dec('100.01') }),
      ],
    });
    const result = await run(h);
    expect(result.rejectCount).toBe(1);
    expect(h.rejects.records[0]?.rejectTranData.startsWith('A000000000000002')).toBe(true);
  });

  it('2700-A – creates a TCATBAL record when the key is missing', async () => {
    const display: string[] = [];
    const h = harness({ trans: [dailyTran({ dalytranTypeCd: '03', dalytranCatCd: '0002', dalytranAmt: dec('-12.34') })] });
    await run(h, display);
    const [bal] = h.tcatbal.entries();
    expect(bal).toMatchObject({ trancatAcctId: ACCT, trancatTypeCd: '03', trancatCd: '0002' });
    expect(bal?.tranCatBal.toFixed(2)).toBe('-12.34');
    expect(display).toContain('TCATBAL record not found for key : 00000000001030002.. Creating.');
  });

  it('2700-B – adds to an existing TCATBAL record', async () => {
    const h = harness({
      trans: [dailyTran({ dalytranAmt: dec('10.00') })],
      tcatbal: [{ trancatAcctId: ACCT, trancatTypeCd: '01', trancatCd: '0001', tranCatBal: dec('5.55') }],
    });
    await run(h);
    expect(h.tcatbal.entries()[0]?.tranCatBal.toFixed(2)).toBe('15.55');
  });

  it('2700-A – INITIALIZE keeps FILLER residue from the previous TCATBAL read', async () => {
    const existing = TRAN_CAT_BAL_LAYOUT.parse('000000000010100010000000000{0000000000000000000000');
    const h = harness({
      trans: [
        dailyTran({ dalytranId: 'A000000000000001' }),
        dailyTran({ dalytranId: 'A000000000000002', dalytranTypeCd: '02' }),
      ],
      tcatbal: [existing],
    });
    await run(h);
    const created = h.tcatbal.entries().find((r) => r.trancatTypeCd === '02');
    expect(TRAN_CAT_BAL_LAYOUT.format(created!).slice(28)).toBe('0'.repeat(22));
  });

  it('category balance overflow truncates high-order digits like COBOL ADD without ON SIZE ERROR', async () => {
    const h = harness({
      trans: [dailyTran({ dalytranAmt: dec('0.02') })],
      accounts: [account({ acctCreditLimit: dec('9999999999.99') })],
      tcatbal: [{ trancatAcctId: ACCT, trancatTypeCd: '01', trancatCd: '0001', tranCatBal: dec('999999999.99') }],
    });
    await run(h);
    expect(h.tcatbal.entries()[0]?.tranCatBal.toFixed(2)).toBe('0.01');
  });
});

describe('abend paths (9999-ABEND-PROGRAM)', () => {
  it('duplicate TRAN-ID on TRANSACT abends with status 22', async () => {
    const display: string[] = [];
    const h = harness({ trans: [dailyTran(), dailyTran()] });
    await expect(run(h, display)).rejects.toBeInstanceOf(AbendError);
    expect(display).toEqual(
      expect.arrayContaining(['ERROR WRITING TO TRANSACTION FILE', 'FILE STATUS IS: NNNN0022', 'ABENDING PROGRAM']),
    );
  });

  it('open failure abends before any processing', async () => {
    const h = harness({ trans: [dailyTran()] });
    h.files.dalytran.open = async () => '35';
    const err = await new Cbtrn02c(h.files, { display: () => {} }).run().catch((e: unknown) => e);
    expect(err).toBeInstanceOf(AbendError);
    expect((err as AbendError).fileStatus).toBe('35');
    expect((err as AbendError).message).toContain('ERROR OPENING DALYTRAN');
  });
});

describe('helpers', () => {
  it('Z-GET-DB2-FORMAT-TIMESTAMP', () => {
    expect(db2FormatTimestamp(new Date(2024, 0, 2, 3, 4, 5, 678))).toBe('2024-01-02-03.04.05.670000');
  });

  it('9910-DISPLAY-IO-STATUS', () => {
    expect(formatIoStatus('23')).toBe('FILE STATUS IS: NNNN0023');
    expect(formatIoStatus('9A')).toBe('FILE STATUS IS: NNNN9065');
  });

  it('RejectReason codes match the COBOL', () => {
    expect(RejectReason).toEqual({
      INVALID_CARD_NUMBER: 100,
      ACCOUNT_NOT_FOUND: 101,
      OVERLIMIT: 102,
      ACCOUNT_EXPIRED: 103,
      ACCOUNT_REWRITE_NOT_FOUND: 109,
    });
  });
});
