import { describe, expect, it } from 'vitest';
import { PROGRAMS, TRANSACTION_IDS } from '../../src/online/routes.js';
import { SIGNON_MESSAGES, signon } from '../../src/online/signonService.js';
import { ADMIN_USER, REGULAR_USER, userRepository } from './fixtures.js';

/** Parity reference: `app/cbl/COSGN00C.cbl` (PROCESS-ENTER-KEY, READ-USER-SEC-FILE). */
describe('COSGN00C signon', () => {
  const users = userRepository();

  it('rejects a blank user id before looking at the password', () => {
    const result = signon({ userId: '   ', password: '' }, users);

    expect(result).toEqual({
      ok: false,
      errorMessage: 'Please enter User ID ...',
      cursorField: 'userId',
    });
  });

  it('rejects a blank password', () => {
    const result = signon({ userId: 'USER0001', password: '  ' }, users);

    expect(result).toEqual({
      ok: false,
      errorMessage: 'Please enter Password ...',
      cursorField: 'password',
    });
  });

  it('reports DFHRESP(NOTFND) on the security file as "User not found"', () => {
    const result = signon({ userId: 'NOSUCH', password: 'PASSWORD' }, users);

    expect(result).toEqual({
      ok: false,
      errorMessage: 'User not found. Try again ...',
      cursorField: 'userId',
    });
  });

  it('reports a password mismatch against SEC-USR-PWD', () => {
    const result = signon({ userId: 'USER0001', password: 'NOTRIGHT' }, users);

    expect(result).toEqual({
      ok: false,
      errorMessage: 'Wrong Password. Try again ...',
      cursorField: 'password',
    });
  });

  it('upper-cases both fields (FUNCTION UPPER-CASE) before the file read', () => {
    const result = signon({ userId: 'user0001', password: 'password' }, users);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.commarea.userId).toBe('USER0001');
  });

  it('XCTLs a regular user to COMEN01C with the COMMAREA the COBOL MOVEs', () => {
    const result = signon({ userId: 'USER0001', password: 'PASSWORD' }, users);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.program).toBe(PROGRAMS.mainMenu);
    expect(result.tranId).toBe(TRANSACTION_IDS.COSGN00C);
    expect(result.commarea).toMatchObject({
      fromTranid: 'CC00',
      fromProgram: 'COSGN00C',
      toProgram: 'COMEN01C',
      userId: REGULAR_USER.secUsrId,
      userType: 'U',
      pgmContext: 0,
    });
  });

  it('XCTLs an admin user (CDEMO-USRTYP-ADMIN) to COADM01C', () => {
    const result = signon({ userId: 'ADMIN001', password: 'PASSWORD' }, users);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.program).toBe(PROGRAMS.adminMenu);
    expect(result.commarea).toMatchObject({
      toProgram: 'COADM01C',
      userId: ADMIN_USER.secUsrId,
      userType: 'A',
    });
  });

  it('leaves the account/customer COMMAREA fields empty at signon', () => {
    const result = signon({ userId: 'USER0001', password: 'PASSWORD' }, users);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.commarea).toMatchObject({
      acctId: '',
      custId: '',
      cardNum: '',
      toTranid: '',
    });
  });

  it('exposes the WS-MESSAGE literals character-for-character', () => {
    expect(SIGNON_MESSAGES).toEqual({
      userIdRequired: 'Please enter User ID ...',
      passwordRequired: 'Please enter Password ...',
      wrongPassword: 'Wrong Password. Try again ...',
      userNotFound: 'User not found. Try again ...',
      thankYou: 'Thank you for using CardDemo application...',
    });
  });
});
