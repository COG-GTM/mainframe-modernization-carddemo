/**
 * Replaces `EXEC CICS XCTL PROGRAM(...)`: instead of transferring control,
 * a service returns the name of the program to navigate to and the caller
 * (HTTP client or UI) resolves it to a route.
 */
export const PROGRAMS = {
  signon: 'COSGN00C',
  mainMenu: 'COMEN01C',
  adminMenu: 'COADM01C',
  accountView: 'COACTVWC',
} as const;

export const TRANSACTION_IDS = {
  COSGN00C: 'CC00',
  COMEN01C: 'CM00',
  COADM01C: 'CA00',
  COACTVWC: 'CAVW',
} as const;

/** Routes the migrated programs are reachable at over HTTP. */
export const PROGRAM_ROUTES: Readonly<Record<string, string>> = {
  COSGN00C: '/signon',
  COMEN01C: '/menu',
  COADM01C: '/admin-menu',
  COACTVWC: '/accounts/view',
};

export function routeForProgram(program: string): string | undefined {
  return PROGRAM_ROUTES[program];
}

/** One entry of CDEMO-MENU-OPT (`app/cpy/COMEN02Y.cpy`). */
export interface MenuOption {
  /** CDEMO-MENU-OPT-NUM PIC 9(02) */
  num: number;
  /** CDEMO-MENU-OPT-NAME PIC X(35) */
  name: string;
  /** CDEMO-MENU-OPT-PGMNAME PIC X(08) */
  pgmName: string;
  /** CDEMO-MENU-OPT-USRTYPE PIC X(01) — 'A' restricts the option to admins */
  usrType: string;
}

/** CARDDEMO-MAIN-MENU-OPTIONS — `app/cpy/COMEN02Y.cpy`. */
export const MAIN_MENU_OPTIONS: readonly MenuOption[] = [
  { num: 1, name: 'Account View', pgmName: 'COACTVWC', usrType: 'U' },
  { num: 2, name: 'Account Update', pgmName: 'COACTUPC', usrType: 'U' },
  { num: 3, name: 'Credit Card List', pgmName: 'COCRDLIC', usrType: 'U' },
  { num: 4, name: 'Credit Card View', pgmName: 'COCRDSLC', usrType: 'U' },
  { num: 5, name: 'Credit Card Update', pgmName: 'COCRDUPC', usrType: 'U' },
  { num: 6, name: 'Transaction List', pgmName: 'COTRN00C', usrType: 'U' },
  { num: 7, name: 'Transaction View', pgmName: 'COTRN01C', usrType: 'U' },
  { num: 8, name: 'Transaction Add', pgmName: 'COTRN02C', usrType: 'U' },
  { num: 9, name: 'Transaction Reports', pgmName: 'CORPT00C', usrType: 'U' },
  { num: 10, name: 'Bill Payment', pgmName: 'COBIL00C', usrType: 'U' },
];

/** CARDDEMO-ADMIN-MENU-OPTIONS — `app/cpy/COADM02Y.cpy`. */
export const ADMIN_MENU_OPTIONS: readonly MenuOption[] = [
  { num: 1, name: 'User List (Security)', pgmName: 'COUSR00C', usrType: 'A' },
  { num: 2, name: 'User Add (Security)', pgmName: 'COUSR01C', usrType: 'A' },
  { num: 3, name: 'User Update (Security)', pgmName: 'COUSR02C', usrType: 'A' },
  { num: 4, name: 'User Delete (Security)', pgmName: 'COUSR03C', usrType: 'A' },
];

/** Programs that have a TypeScript handler; everything else is "coming soon". */
export const IMPLEMENTED_PROGRAMS: ReadonlySet<string> = new Set<string>([
  PROGRAMS.signon,
  PROGRAMS.mainMenu,
  PROGRAMS.accountView,
]);
