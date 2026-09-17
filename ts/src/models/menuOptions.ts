/**
 * Menu option tables.
 *
 * CARDDEMO-MAIN-MENU-OPTIONS (copybook COMEN02Y): 10 options, each
 * 9(02) number + X(35) name + X(08) program name + X(01) user type.
 * CARDDEMO-ADMIN-MENU-OPTIONS (copybook COADM02Y): 4 options, each
 * 9(02) number + X(35) name + X(08) program name.
 */

export interface MenuOption {
  optNum: number;
  optName: string;
  optPgmName: string;
}

export interface MainMenuOption extends MenuOption {
  /** CDEMO-MENU-OPT-USRTYPE: 'U' for regular user functions. */
  optUsrType: string;
}

export const MENU_OPT_NAME_LENGTH = 35;
export const MENU_OPT_PGMNAME_LENGTH = 8;

export const CDEMO_MENU_OPT_COUNT = 10;

export const CARDDEMO_MAIN_MENU_OPTIONS: readonly MainMenuOption[] = [
  { optNum: 1, optName: 'Account View', optPgmName: 'COACTVWC', optUsrType: 'U' },
  { optNum: 2, optName: 'Account Update', optPgmName: 'COACTUPC', optUsrType: 'U' },
  { optNum: 3, optName: 'Credit Card List', optPgmName: 'COCRDLIC', optUsrType: 'U' },
  { optNum: 4, optName: 'Credit Card View', optPgmName: 'COCRDSLC', optUsrType: 'U' },
  { optNum: 5, optName: 'Credit Card Update', optPgmName: 'COCRDUPC', optUsrType: 'U' },
  { optNum: 6, optName: 'Transaction List', optPgmName: 'COTRN00C', optUsrType: 'U' },
  { optNum: 7, optName: 'Transaction View', optPgmName: 'COTRN01C', optUsrType: 'U' },
  { optNum: 8, optName: 'Transaction Add', optPgmName: 'COTRN02C', optUsrType: 'U' },
  { optNum: 9, optName: 'Transaction Reports', optPgmName: 'CORPT00C', optUsrType: 'U' },
  { optNum: 10, optName: 'Bill Payment', optPgmName: 'COBIL00C', optUsrType: 'U' },
];

export const CDEMO_ADMIN_OPT_COUNT = 4;

export const CARDDEMO_ADMIN_MENU_OPTIONS: readonly MenuOption[] = [
  { optNum: 1, optName: 'User List (Security)', optPgmName: 'COUSR00C' },
  { optNum: 2, optName: 'User Add (Security)', optPgmName: 'COUSR01C' },
  { optNum: 3, optName: 'User Update (Security)', optPgmName: 'COUSR02C' },
  { optNum: 4, optName: 'User Delete (Security)', optPgmName: 'COUSR03C' },
];
