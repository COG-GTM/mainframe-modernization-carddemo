import type { KeyedRepository } from '../data/repositories.js';
import type { SecUserRecord } from '../domain/user.js';
import { USER_TYPE_ADMIN } from '../domain/user.js';
import type { CardDemoCommarea } from '../domain/commarea.js';
import { emptyCommarea } from '../domain/commarea.js';
import { PROGRAMS, TRANSACTION_IDS } from './routes.js';

/** Messages moved to WS-MESSAGE / ERRMSGO in `app/cbl/COSGN00C.cbl`. */
export const SIGNON_MESSAGES = {
  userIdRequired: 'Please enter User ID ...',
  passwordRequired: 'Please enter Password ...',
  wrongPassword: 'Wrong Password. Try again ...',
  userNotFound: 'User not found. Try again ...',
  thankYou: 'Thank you for using CardDemo application...',
} as const;

export interface SignonRequest {
  /** USERIDI OF COSGN0AI — BMS map COSGN00, 8 characters */
  userId: string;
  /** PASSWDI OF COSGN0AI — BMS map COSGN00, 8 characters */
  password: string;
}

export type SignonResult =
  | { ok: true; program: string; tranId: string; commarea: CardDemoCommarea }
  | { ok: false; errorMessage: string; cursorField: 'userId' | 'password' };

/**
 * `COSGN00C` — PROCESS-ENTER-KEY + READ-USER-SEC-FILE.
 *
 * `EXEC CICS XCTL` to COADM01C / COMEN01C becomes the returned `program`,
 * and the COMMAREA it would have carried becomes the returned session state.
 */
export function signon(
  request: SignonRequest,
  users: KeyedRepository<SecUserRecord>,
): SignonResult {
  if (request.userId.trim() === '') {
    return { ok: false, errorMessage: SIGNON_MESSAGES.userIdRequired, cursorField: 'userId' };
  }
  if (request.password.trim() === '') {
    return { ok: false, errorMessage: SIGNON_MESSAGES.passwordRequired, cursorField: 'password' };
  }

  const userId = request.userId.trim().toUpperCase();
  const password = request.password.trim().toUpperCase();

  const user = users.read(userId);
  if (user === undefined) {
    return { ok: false, errorMessage: SIGNON_MESSAGES.userNotFound, cursorField: 'userId' };
  }
  if (user.secUsrPwd.trim() !== password) {
    return { ok: false, errorMessage: SIGNON_MESSAGES.wrongPassword, cursorField: 'password' };
  }

  const program = user.secUsrType === USER_TYPE_ADMIN ? PROGRAMS.adminMenu : PROGRAMS.mainMenu;
  const commarea: CardDemoCommarea = {
    ...emptyCommarea(),
    fromTranid: TRANSACTION_IDS.COSGN00C,
    fromProgram: PROGRAMS.signon,
    toProgram: program,
    userId,
    userType: user.secUsrType,
    pgmContext: 0,
  };

  return { ok: true, program, tranId: TRANSACTION_IDS.COSGN00C, commarea };
}
