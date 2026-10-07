/**
 * Port of CSUTLDTC.cbl: confirms a CCYYMMDD date is a real calendar date.
 * The original calls the LE service CEEDAYS; here a native `Date` round-trip
 * (build the UTC date, read the parts back, compare) gives the same answer.
 */

export type CalendarCheckResult =
  | 'Date is valid'
  | 'Nonnumeric data'
  | 'Datevalue error'
  | 'Unsupp. Range';

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
/** CEEDAYS FC-UNSUPP-RANGE (X'0003 09D1'). */
const CEE_UNSUPP_RANGE = { severity: 3, msgNo: 0x09d1 } as const;

/** CEEDAYS only handles Lilian dates: 15 Oct 1582 through 31 Dec 9999. */
const LILIAN_START = 15821015;

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

  if (!roundTrips) {
    return { ...CEE_BAD_DATE_VALUE, result: 'Datevalue error' };
  }
  if (Number(ccyymmdd) < LILIAN_START) {
    return { ...CEE_UNSUPP_RANGE, result: 'Unsupp. Range' };
  }
  return { severity: 0, msgNo: 0, result: 'Date is valid' };
}
