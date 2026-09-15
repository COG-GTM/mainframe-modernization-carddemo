import { describe, expect, it } from 'vitest';
import {
  MENU_MESSAGES,
  comingSoonMessage,
  selectMenuOption,
} from '../../src/online/menuService.js';
import {
  ADMIN_MENU_OPTIONS,
  MAIN_MENU_OPTIONS,
  PROGRAMS,
  type MenuOption,
} from '../../src/online/routes.js';
import { signedOnCommarea } from './fixtures.js';

/** Parity reference: `app/cbl/COMEN01C.cbl` + `app/cpy/COMEN02Y.cpy`. */
describe('COMEN01C menu selection', () => {
  const user = signedOnCommarea('U');
  const admin = signedOnCommarea('A');

  it.each(['X', 'ab', '1A', ''])('rejects the non-numeric option %j', (rawOption) => {
    const result = selectMenuOption(rawOption, user);

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe('Please enter a valid option number...');
  });

  it('rejects option zero (WS-OPTION = ZEROS)', () => {
    const result = selectMenuOption('00', user);

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe(MENU_MESSAGES.invalidOption);
  });

  it('rejects an option above CDEMO-MENU-OPT-COUNT', () => {
    expect(MAIN_MENU_OPTIONS).toHaveLength(10);

    const result = selectMenuOption('11', user);

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe(MENU_MESSAGES.invalidOption);
  });

  it('pads a blank-prefixed option the way INSPECT ... REPLACING does', () => {
    const result = selectMenuOption(' 1', user);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.program).toBe(PROGRAMS.accountView);
  });

  it('blocks a regular user from an admin-only (USRTYPE = A) option', () => {
    const adminOption = ADMIN_MENU_OPTIONS[0] as MenuOption;
    expect(adminOption.usrType).toBe('A');

    const result = selectMenuOption('1', user, ADMIN_MENU_OPTIONS);

    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.errorMessage).toBe('No access - Admin Only option...');
    expect(result.commarea).toBe(user);
  });

  it('lets an admin through the same admin-only option', () => {
    const adminOnlyImplemented: readonly MenuOption[] = [
      { num: 1, name: 'Account View', pgmName: PROGRAMS.accountView, usrType: 'A' },
    ];

    const result = selectMenuOption('1', admin, adminOnlyImplemented);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.program).toBe(PROGRAMS.accountView);
  });

  it('XCTLs to an implemented program and updates the COMMAREA', () => {
    const result = selectMenuOption('1', user);

    expect(result.ok).toBe(true);
    if (!result.ok) return;
    expect(result.program).toBe('COACTVWC');
    expect(result.option).toEqual(MAIN_MENU_OPTIONS[0]);
    expect(result.commarea).toMatchObject({
      fromTranid: 'CM00',
      fromProgram: 'COMEN01C',
      toProgram: 'COACTVWC',
      pgmContext: 0,
      userId: user.userId,
      userType: 'U',
    });
  });

  it('returns the "coming soon" message for an option with no TypeScript handler', () => {
    const result = selectMenuOption('2', user);

    expect(result.ok).toBe(false);
    if (result.ok) return;
    // STRING ... DELIMITED BY SPACE copies only 'Account' out of 'Account Update'.
    expect(result.errorMessage).toBe('This option Accountis coming soon ...');
    expect(result.errorMessage).toBe(comingSoonMessage(MAIN_MENU_OPTIONS[1] as MenuOption));
  });

  it('keeps the COMEN02Y option table in copybook order', () => {
    expect(MAIN_MENU_OPTIONS.map((option) => option.pgmName)).toEqual([
      'COACTVWC',
      'COACTUPC',
      'COCRDLIC',
      'COCRDSLC',
      'COCRDUPC',
      'COTRN00C',
      'COTRN01C',
      'COTRN02C',
      'CORPT00C',
      'COBIL00C',
    ]);
    expect(MAIN_MENU_OPTIONS.every((option) => option.usrType === 'U')).toBe(true);
  });
});
