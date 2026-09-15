import type { CardDemoCommarea } from '../domain/commarea.js';
import { isAdmin } from '../domain/commarea.js';
import {
  IMPLEMENTED_PROGRAMS,
  MAIN_MENU_OPTIONS,
  PROGRAMS,
  TRANSACTION_IDS,
  type MenuOption,
} from './routes.js';

/** Messages moved to WS-MESSAGE / ERRMSGO in `app/cbl/COMEN01C.cbl`. */
export const MENU_MESSAGES = {
  invalidOption: 'Please enter a valid option number...',
  adminOnly: 'No access - Admin Only option...',
} as const;

export function comingSoonMessage(option: MenuOption): string {
  return `This option ${option.name} is coming soon ...`;
}

export type MenuSelection =
  | { ok: true; program: string; option: MenuOption; commarea: CardDemoCommarea }
  | { ok: false; errorMessage: string; commarea: CardDemoCommarea };

/**
 * `COMEN01C` — PROCESS-ENTER-KEY.
 *
 * The COBOL pads the received option with zeros (`INSPECT ... REPLACING ALL ' '
 * BY '0'`) before the numeric edit, so ' 1' and '1' both select option 1.
 */
export function selectMenuOption(
  rawOption: string,
  commarea: CardDemoCommarea,
  options: readonly MenuOption[] = MAIN_MENU_OPTIONS,
): MenuSelection {
  const normalized = rawOption.trimEnd().replace(/ /g, '0');
  const isNumeric = /^\d+$/.test(normalized);
  const optionNumber = isNumeric ? Number(normalized) : Number.NaN;

  if (!isNumeric || optionNumber === 0 || optionNumber > options.length) {
    return { ok: false, errorMessage: MENU_MESSAGES.invalidOption, commarea };
  }

  const option = options[optionNumber - 1] as MenuOption;

  if (!isAdmin(commarea) && option.usrType === 'A') {
    return { ok: false, errorMessage: MENU_MESSAGES.adminOnly, commarea };
  }

  if (!IMPLEMENTED_PROGRAMS.has(option.pgmName)) {
    return { ok: false, errorMessage: comingSoonMessage(option), commarea };
  }

  return {
    ok: true,
    program: option.pgmName,
    option,
    commarea: {
      ...commarea,
      fromTranid: TRANSACTION_IDS.COMEN01C,
      fromProgram: PROGRAMS.mainMenu,
      toProgram: option.pgmName,
      pgmContext: 0,
    },
  };
}

export function menuOptionLabels(options: readonly MenuOption[] = MAIN_MENU_OPTIONS): string[] {
  return options.map((option) => `${String(option.num).padStart(2, '0')}. ${option.name}`);
}
