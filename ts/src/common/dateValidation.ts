/**
 * Date edits migrated from CSUTLDPY.cpy (procedure division copybook) and its
 * working storage CSUTLDWY.cpy. The paragraphs EDIT-YEAR-CCYY, EDIT-MONTH,
 * EDIT-DAY, EDIT-DAY-MONTH-YEAR and EDIT-DATE-OF-BIRTH are reproduced with the
 * original messages and flag semantics.
 */

export type DateFieldFlag = 'valid' | 'notOk' | 'blank';

export interface DateEditResult {
  valid: boolean;
  yearFlag: DateFieldFlag;
  monthFlag: DateFieldFlag;
  dayFlag: DateFieldFlag;
  /** WS-RETURN-MSG: only the first failure sets a message, as in COBOL. */
  returnMsg: string;
}

function isBlank(value: string): boolean {
  return value.trim() === '' || /^\0+$/.test(value);
}

function isNumeric(value: string): boolean {
  return /^\d+$/.test(value);
}

/**
 * EDIT-DATE-CCYYMMDD: validates a CCYYMMDD string.
 * `variableName` is WS-EDIT-VARIABLE-NAME, used to build the messages.
 */
export function editDateCcyymmdd(ccyymmdd: string, variableName: string): DateEditResult {
  const result: DateEditResult = {
    valid: false,
    yearFlag: 'notOk',
    monthFlag: 'notOk',
    dayFlag: 'notOk',
    returnMsg: '',
  };
  const name = variableName.trim();
  const padded = ccyymmdd.padEnd(8, ' ');
  const ccyy = padded.slice(0, 4);
  const mm = padded.slice(4, 6);
  const dd = padded.slice(6, 8);

  // EDIT-YEAR-CCYY
  if (isBlank(ccyy)) {
    result.yearFlag = 'blank';
    result.returnMsg = `${name} : Year must be supplied.`;
    return result;
  }
  if (!isNumeric(ccyy)) {
    result.returnMsg = `${name} must be 4 digit number.`;
    return result;
  }
  const century = Number(ccyy.slice(0, 2));
  if (century !== 20 && century !== 19) {
    result.returnMsg = `${name} : Century is not valid.`;
    return result;
  }
  result.yearFlag = 'valid';

  // EDIT-MONTH
  if (isBlank(mm)) {
    result.monthFlag = 'blank';
    result.returnMsg = `${name} : Month must be supplied.`;
    return result;
  }
  const monthNum = Number(mm);
  if (!isNumeric(mm) || !(monthNum >= 1 && monthNum <= 12)) {
    result.returnMsg = `${name}: Month must be a number between 1 and 12.`;
    return result;
  }
  result.monthFlag = 'valid';

  // EDIT-DAY
  if (isBlank(dd)) {
    result.dayFlag = 'blank';
    result.returnMsg = `${name} : Day must be supplied.`;
    return result;
  }
  const dayNum = Number(dd);
  if (!isNumeric(dd) || !(dayNum >= 1 && dayNum <= 31)) {
    result.returnMsg = `${name}:day must be a number between 1 and 31.`;
    return result;
  }
  result.dayFlag = 'valid';

  // EDIT-DAY-MONTH-YEAR
  const is31DayMonth = [1, 3, 5, 7, 8, 10, 12].includes(monthNum);
  if (!is31DayMonth && dayNum === 31) {
    result.dayFlag = 'notOk';
    result.monthFlag = 'notOk';
    result.returnMsg = `${name}:Cannot have 31 days in this month.`;
    return result;
  }
  if (monthNum === 2 && dayNum === 30) {
    result.dayFlag = 'notOk';
    result.monthFlag = 'notOk';
    result.returnMsg = `${name}:Cannot have 30 days in this month.`;
    return result;
  }
  if (monthNum === 2 && dayNum === 29) {
    const yy = Number(ccyy.slice(2, 4));
    const divisor = yy === 0 ? 400 : 4;
    if (Number(ccyy) % divisor !== 0) {
      result.dayFlag = 'notOk';
      result.monthFlag = 'notOk';
      result.yearFlag = 'notOk';
      result.returnMsg = `${name}:Not a leap year.Cannot have 29 days in this month.`;
      return result;
    }
  }

  result.valid = true;
  return result;
}

/** EDIT-DATE-OF-BIRTH: the date must already be valid and be in the past. */
export function editDateOfBirth(
  ccyymmdd: string,
  variableName: string,
  now: Date = new Date(),
): DateEditResult {
  const result = editDateCcyymmdd(ccyymmdd, variableName);
  if (!result.valid) {
    return result;
  }
  const name = variableName.trim();
  const today = now.getFullYear() * 10000 + (now.getMonth() + 1) * 100 + now.getDate();
  if (today > Number(ccyymmdd)) {
    return result;
  }
  return {
    valid: false,
    yearFlag: 'notOk',
    monthFlag: 'notOk',
    dayFlag: 'notOk',
    returnMsg: `${name}:cannot be in the future `,
  };
}
