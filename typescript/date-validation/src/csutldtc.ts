/**
 * Port of CSUTLDTC.cbl: confirms a CCYYMMDD date is a real calendar date.
 * The original calls the LE service CEEDAYS; here a native `Date` round-trip
 * (build the UTC date, read the parts back, compare) gives the same answer.
 */

export type CalendarCheckResult =
  | 'Date is valid'
  | 'Nonnumeric data'
  | 'Datevalue error';

export interface CalendarCheck {
  /** CEEDAYS feedback severity (WS-SEVERITY): 0 = valid, 3 = error. */
  severity: number;
  /** CEEDAYS message number (WS-MSG-NO): 0 when valid. */
  msgNo: number;
  /** WS-RESULT text. */
  result: CalendarCheckResult;
}

/** CEEDAYS FC-NON-NUMERIC-DATA (X'0003 09D8'). */
const CEE_NON_NUMERIC = { severity: 3, msgNo: 0x09d8 } as const;
/** CEEDAYS FC-BAD-DATE-VALUE (X'0003 09CC'). */
const CEE_BAD_DATE_VALUE = { severity: 3, msgNo: 0x09cc } as const;

export function checkCalendarDate(ccyymmdd: string): CalendarCheck {
  if (!/^\d{8}$/.test(ccyymmdd)) {
    return { ...CEE_NON_NUMERIC, result: 'Nonnumeric data' };
  }
  const year = Number(ccyymmdd.slice(0, 4));
  const month = Number(ccyymmdd.slice(4, 6));
  const day = Number(ccyymmdd.slice(6, 8));

  const d = new Date(0);
  d.setUTCFullYear(year, month - 1, day);
  const roundTrips =
    d.getUTCFullYear() === year &&
    d.getUTCMonth() === month - 1 &&
    d.getUTCDate() === day;

  return roundTrips
    ? { severity: 0, msgNo: 0, result: 'Date is valid' }
    : { ...CEE_BAD_DATE_VALUE, result: 'Datevalue error' };
}
