/**
 * Port of the CardDemo date edit routines:
 *   CSUTLDWY.cpy - CCYYMMDD working storage, validity flags
 *   CSUTLDPY.cpy - EDIT-DATE-CCYYMMDD and EDIT-DATE-OF-BIRTH paragraphs
 *   CSUTLDTC.cbl - CEEDAYS calendar check (see ./csutldtc.ts)
 *
 * Control flow follows the COBOL: the year, month and day edits always all
 * run (their GO TOs only skip to the end of the current paragraph), only the
 * first error message is kept (WS-RETURN-MSG-OFF), and the cross-field and
 * calendar checks stop at the first failure.
 */

import { checkCalendarDate } from './csutldtc.js';

/** Per-field flag: FLG-xxx-ISVALID (LOW-VALUES) / FLG-xxx-NOT-OK ('0') / FLG-xxx-BLANK ('B'). */
export type FieldFlag = 'VALID' | 'NOT_OK' | 'BLANK';

export interface DateFieldFlags {
  year: FieldFlag;
  month: FieldFlag;
  day: FieldFlag;
}

export interface DateValidationResult {
  /** True when no edit raised INPUT-ERROR. */
  valid: boolean;
  /** WS-EDIT-DATE-FLGS. All three VALID = WS-EDIT-DATE-IS-VALID. */
  flags: DateFieldFlags;
  /** WS-RETURN-MSG: first error message, or '' when valid. */
  message: string;
}

export interface ValidateDateOptions {
  /** WS-EDIT-VARIABLE-NAME, prefixed to every message. Default 'Date'. */
  fieldName?: string;
}

export interface ValidateDateOfBirthOptions extends ValidateDateOptions {
  /** Stand-in for FUNCTION CURRENT-DATE (local date). Default: now. */
  today?: Date;
}

/** Message literals from the COBOL STRING statements, verbatim. */
export const MESSAGES = {
  yearBlank: ' : Year must be supplied.',
  yearNotNumeric: ' must be 4 digit number.',
  centuryInvalid: ' : Century is not valid.',
  monthBlank: ' : Month must be supplied.',
  monthInvalid: ': Month must be a number between 1 and 12.',
  dayBlank: ' : Day must be supplied.',
  dayInvalid: ':day must be a number between 1 and 31.',
  day31: ':Cannot have 31 days in this month.',
  feb30: ':Cannot have 30 days in this month.',
  notLeapYear: ':Not a leap year.Cannot have 29 days in this month.',
  future: ':cannot be in the future ',
} as const;

/** WS-EDIT-DATE-CCYYMMDD is PIC X(8). */
const DATE_LENGTH = 8;
/** WS-RETURN-MSG is PIC X(75) in the calling program (COACTUPC). */
const RETURN_MSG_LENGTH = 75;
/** WS-EDIT-VARIABLE-NAME is PIC X(25). */
const VARIABLE_NAME_LENGTH = 25;

const THIRTY_ONE_DAY_MONTHS = new Set([1, 3, 5, 7, 8, 10, 12]);
const FEBRUARY = 2;

class EditContext {
  valid = true;
  message = '';
  readonly flags: DateFieldFlags = { year: 'NOT_OK', month: 'NOT_OK', day: 'NOT_OK' };

  constructor(private readonly fieldName: string) {}

  /** SET INPUT-ERROR + STRING ... INTO WS-RETURN-MSG when WS-RETURN-MSG-OFF. */
  error(literal: string): void {
    this.valid = false;
    if (this.message === '') {
      this.message = (this.fieldName + literal).slice(0, RETURN_MSG_LENGTH).trimEnd();
    }
  }

  allNotOk(): void {
    this.flags.year = 'NOT_OK';
    this.flags.month = 'NOT_OK';
    this.flags.day = 'NOT_OK';
  }

  get flagsAllValid(): boolean {
    return this.flags.year === 'VALID' && this.flags.month === 'VALID' && this.flags.day === 'VALID';
  }

  result(): DateValidationResult {
    return { valid: this.valid, flags: { ...this.flags }, message: this.message };
  }
}

/** MOVE to PIC X(n): right-pad with spaces, truncate. */
function toPicX(value: string, length: number): string {
  return value.padEnd(length, ' ').slice(0, length);
}

/** EQUAL SPACES or EQUAL LOW-VALUES. */
function isBlank(field: string): boolean {
  return /^ *$/.test(field) || /^\0*$/.test(field);
}

function isDigits(field: string): boolean {
  return /^\d+$/.test(field);
}

/** Numeric value of a PIC 9 redefine, or undefined when the bytes are not digits. */
function numericValue(field: string): number | undefined {
  return isDigits(field) ? Number(field) : undefined;
}

/** EDIT-YEAR-CCYY */
function editYear(ctx: EditContext, ccyy: string): void {
  ctx.flags.year = 'NOT_OK';
  if (isBlank(ccyy)) {
    ctx.flags.year = 'BLANK';
    ctx.error(MESSAGES.yearBlank);
    return;
  }
  if (!isDigits(ccyy)) {
    ctx.error(MESSAGES.yearNotNumeric);
    return;
  }
  // 88 THIS-CENTURY VALUE 20 / LAST-CENTURY VALUE 19: only 19xx and 20xx accepted.
  const cc = Number(ccyy.slice(0, 2));
  if (cc !== 19 && cc !== 20) {
    ctx.error(MESSAGES.centuryInvalid);
    return;
  }
  ctx.flags.year = 'VALID';
}

/** EDIT-MONTH */
function editMonth(ctx: EditContext, mm: string): void {
  ctx.flags.month = 'NOT_OK';
  if (isBlank(mm)) {
    ctx.flags.month = 'BLANK';
    ctx.error(MESSAGES.monthBlank);
    return;
  }
  // 88 WS-VALID-MONTH VALUES 1 THROUGH 12, then TEST-NUMVAL; both use the same message.
  // The 88 test reads the raw bytes as PIC 9(2), so only two digits can pass it.
  const month = numericValue(mm);
  if (month === undefined || month < 1 || month > 12) {
    ctx.error(MESSAGES.monthInvalid);
    return;
  }
  ctx.flags.month = 'VALID';
}

/**
 * FUNCTION TEST-NUMVAL / NUMVAL on a short field: optional spaces, one leading
 * or trailing sign (+, -, CR, DB) and an optional decimal point. Returns the
 * value as it lands in an unsigned PIC 9(2) (sign dropped, fraction and
 * high-order digits truncated), or undefined when TEST-NUMVAL fails.
 */
function numvalToPic99(field: string): number | undefined {
  const num = String.raw`(\d+\.?\d*|\.\d+)`;
  const leadingSign = new RegExp(`^ *([+-])? *${num} *$`);
  const trailingSign = new RegExp(`^ *${num} *([+-]|CR|DB)? *$`);
  const match = leadingSign.exec(field) ?? trailingSign.exec(field);
  if (match === null) {
    return undefined;
  }
  const digits = match.slice(1).find((g) => g !== undefined && /\d/.test(g)) ?? '0';
  return Math.trunc(Number(digits)) % 100;
}

/**
 * EDIT-DAY. Returns the day field as the COBOL leaves it: on TEST-NUMVAL
 * success it is overwritten with NUMVAL, so '5 ', ' 5', '+5' and '5-' all
 * become '05' and are used that way by the later checks.
 */
function editDay(ctx: EditContext, dd: string): string {
  ctx.flags.day = 'VALID';
  if (isBlank(dd)) {
    ctx.flags.day = 'BLANK';
    ctx.error(MESSAGES.dayBlank);
    return dd;
  }
  const day = numvalToPic99(dd);
  if (day === undefined) {
    ctx.flags.day = 'NOT_OK';
    ctx.error(MESSAGES.dayInvalid);
    return dd;
  }
  const normalized = String(day).padStart(2, '0');
  // 88 WS-VALID-DAY VALUES 1 THROUGH 31
  if (day < 1 || day > 31) {
    ctx.flags.day = 'NOT_OK';
    ctx.error(MESSAGES.dayInvalid);
  }
  return normalized;
}

/**
 * EDIT-DAY-MONTH-YEAR. Runs even when a field edit failed (as in the COBOL);
 * a check is skipped when the field it needs is not numeric. Returns false
 * when the COBOL would GO TO EDIT-DATE-CCYYMMDD-EXIT.
 */
function editDayMonthYear(ctx: EditContext, ccyy: string, mm: string, dd: string): boolean {
  const year = numericValue(ccyy);
  const month = numericValue(mm);
  const day = numericValue(dd);

  if (month !== undefined && day === 31 && !THIRTY_ONE_DAY_MONTHS.has(month)) {
    ctx.flags.day = 'NOT_OK';
    ctx.flags.month = 'NOT_OK';
    ctx.error(MESSAGES.day31);
    return false;
  }

  if (month === FEBRUARY && day === 30) {
    ctx.flags.day = 'NOT_OK';
    ctx.flags.month = 'NOT_OK';
    ctx.error(MESSAGES.feb30);
    return false;
  }

  if (month === FEBRUARY && day === 29 && year !== undefined) {
    // Century years (YY = 00) must divide by 400, all others by 4.
    const yy = year % 100;
    const divBy = yy === 0 ? 400 : 4;
    if (year % divBy !== 0) {
      ctx.allNotOk();
      ctx.error(MESSAGES.notLeapYear);
      return false;
    }
  }

  // IF WS-EDIT-DATE-IS-VALID CONTINUE ELSE GO TO EDIT-DATE-CCYYMMDD-EXIT
  return ctx.flagsAllValid;
}

/** EDIT-DATE-LE: CALL 'CSUTLDTC' (CEEDAYS) as a last-resort calendar check. */
function editDateLe(ctx: EditContext, ccyymmdd: string): void {
  const check = checkCalendarDate(ccyymmdd);
  if (check.severity === 0) {
    return;
  }
  // The COBOL falls through to SET WS-EDIT-DATE-IS-VALID after this error,
  // clearing the flags; we keep them NOT_OK so `flags` agrees with `valid`.
  ctx.allNotOk();
  const sev = String(check.severity).padStart(4, '0');
  const msgNo = String(check.msgNo).padStart(4, '0');
  ctx.error(` validation error Sev code: ${sev} Message code: ${msgNo}`);
}

interface DateEditRun {
  ctx: EditContext;
  /** WS-EDIT-DATE-CCYYMMDD after the edits (day field normalized by NUMVAL). */
  date: string;
}

function runDateEdits(ccyymmdd: string, fieldName: string): DateEditRun {
  const ctx = new EditContext(toPicX(fieldName, VARIABLE_NAME_LENGTH).trim());
  const input = toPicX(ccyymmdd, DATE_LENGTH);
  const ccyy = input.slice(0, 4);
  const mm = input.slice(4, 6);

  editYear(ctx, ccyy);
  editMonth(ctx, mm);
  const dd = editDay(ctx, input.slice(6, 8));
  const date = ccyy + mm + dd;
  if (editDayMonthYear(ctx, ccyy, mm, dd)) {
    editDateLe(ctx, date);
  }
  return { ctx, date };
}

/**
 * EDIT-DATE-CCYYMMDD: validates an 8-character CCYYMMDD date.
 * Input is treated like a MOVE into PIC X(8): shorter values are
 * space-padded (so '' is fully blank), longer values are truncated.
 */
export function validateDate(ccyymmdd: string, options: ValidateDateOptions = {}): DateValidationResult {
  return runDateEdits(ccyymmdd, options.fieldName ?? 'Date').ctx.result();
}

/** CCYYMMDD as an integer for the local calendar date (FUNCTION CURRENT-DATE). */
function localCcyymmdd(date: Date): number {
  return date.getFullYear() * 10000 + (date.getMonth() + 1) * 100 + date.getDate();
}

/**
 * EDIT-DATE-CCYYMMDD followed, when the date is valid, by EDIT-DATE-OF-BIRTH
 * (as COACTUPC does). The date of birth must be strictly before today; a DOB
 * of today is rejected, matching `IF WS-CURRENT-DATE-BINARY > WS-EDIT-DATE-BINARY`.
 */
export function validateDateOfBirth(
  ccyymmdd: string,
  options: ValidateDateOfBirthOptions = {},
): DateValidationResult {
  const { ctx, date } = runDateEdits(ccyymmdd, options.fieldName ?? 'Date of Birth');
  if (!ctx.flagsAllValid) {
    return ctx.result();
  }
  const dob = Number(date);
  const today = localCcyymmdd(options.today ?? new Date());
  if (!(today > dob)) {
    ctx.allNotOk();
    ctx.error(MESSAGES.future);
  }
  return ctx.result();
}
