import { describe, expect, it } from 'vitest';
import { checkCalendarDate, validateDate, validateDateOfBirth } from '../src/index.js';

const ALL_VALID = { year: 'VALID', month: 'VALID', day: 'VALID' } as const;
const ALL_NOT_OK = { year: 'NOT_OK', month: 'NOT_OK', day: 'NOT_OK' } as const;

describe('validateDate', () => {
  describe('valid dates', () => {
    it.each(['20230115', '19991231', '19000101', '20991231', '20240229', '20000229', '20230430'])(
      '%s is valid',
      (date) => {
        expect(validateDate(date)).toEqual({ valid: true, flags: ALL_VALID, message: '' });
      },
    );
  });

  describe('year (EDIT-YEAR-CCYY)', () => {
    it('blank year sets FLG-YEAR-BLANK', () => {
      expect(validateDate('    0115')).toEqual({
        valid: false,
        flags: { year: 'BLANK', month: 'VALID', day: 'VALID' },
        message: 'Date : Year must be supplied.',
      });
    });

    it('LOW-VALUES year is treated as blank', () => {
      expect(validateDate('\0\0\0\x000115').flags.year).toBe('BLANK');
    });

    it.each(['20A3', '2 23', '-023'])('non-numeric year %s', (ccyy) => {
      expect(validateDate(`${ccyy}0115`)).toEqual({
        valid: false,
        flags: { year: 'NOT_OK', month: 'VALID', day: 'VALID' },
        message: 'Date must be 4 digit number.',
      });
    });

    it.each(['1899', '2100', '0001'])('century outside 19/20 (%s)', (ccyy) => {
      expect(validateDate(`${ccyy}0115`)).toEqual({
        valid: false,
        flags: { year: 'NOT_OK', month: 'VALID', day: 'VALID' },
        message: 'Date : Century is not valid.',
      });
    });
  });

  describe('month (EDIT-MONTH)', () => {
    it('blank month sets FLG-MONTH-BLANK', () => {
      expect(validateDate('2023  15')).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'BLANK', day: 'VALID' },
        message: 'Date : Month must be supplied.',
      });
    });

    it.each(['00', '13', '99', 'AB', '1A'])('invalid month %s', (mm) => {
      expect(validateDate(`2023${mm}15`)).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'NOT_OK', day: 'VALID' },
        message: 'Date: Month must be a number between 1 and 12.',
      });
    });
  });

  describe('day (EDIT-DAY)', () => {
    it('blank day sets FLG-DAY-BLANK', () => {
      expect(validateDate('202301  ')).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'VALID', day: 'BLANK' },
        message: 'Date : Day must be supplied.',
      });
    });

    it.each(['00', '32', 'XX', '3X'])('invalid day %s', (dd) => {
      expect(validateDate(`202301${dd}`)).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'VALID', day: 'NOT_OK' },
        message: 'Date:day must be a number between 1 and 31.',
      });
    });
  });

  describe('day NUMVAL normalization (COMPUTE WS-EDIT-DATE-DD-N = NUMVAL)', () => {
    it.each(['202301 5', '2023015 ', '202301+5', '2023015-', '202301-5', '2023015.'])('%s is read as day 05', (date) => {
      expect(validateDate(date)).toEqual({ valid: true, flags: ALL_VALID, message: '' });
    });

    it('normalized day feeds the later checks (April 3-)', () => {
      expect(validateDate('2023043-').valid).toBe(true);
    });

    it.each(['202301.5', '202301-0'])('%s truncates to day 00 and fails', (date) => {
      expect(validateDate(date).message).toBe('Date:day must be a number between 1 and 31.');
    });
  });

  describe('blank and short input', () => {
    it('empty string: every field blank, first message wins', () => {
      expect(validateDate('')).toEqual({
        valid: false,
        flags: { year: 'BLANK', month: 'BLANK', day: 'BLANK' },
        message: 'Date : Year must be supplied.',
      });
    });

    it('all spaces behaves like empty string', () => {
      expect(validateDate('        ')).toEqual(validateDate(''));
    });

    it('short input is space-padded like MOVE to PIC X(8)', () => {
      expect(validateDate('202301')).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'VALID', day: 'BLANK' },
        message: 'Date : Day must be supplied.',
      });
    });

    it('long input is truncated to 8 characters', () => {
      expect(validateDate('20230115XYZ').valid).toBe(true);
    });
  });

  describe('non-numeric input', () => {
    it('fully non-numeric: all fields NOT_OK, year message first', () => {
      expect(validateDate('ABCDEFGH')).toEqual({
        valid: false,
        flags: ALL_NOT_OK,
        message: 'Date must be 4 digit number.',
      });
    });

    it('ISO-formatted date is rejected on the month', () => {
      expect(validateDate('2023-01-15')).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'NOT_OK', day: 'VALID' },
        message: 'Date: Month must be a number between 1 and 12.',
      });
    });
  });

  describe('30/31-day months (EDIT-DAY-MONTH-YEAR)', () => {
    it.each(['04', '06', '09', '11', '02'])('day 31 in month %s', (mm) => {
      expect(validateDate(`2023${mm}31`)).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'NOT_OK', day: 'NOT_OK' },
        message: 'Date:Cannot have 31 days in this month.',
      });
    });

    it.each(['01', '03', '05', '07', '08', '10', '12'])('day 31 in month %s is fine', (mm) => {
      expect(validateDate(`2023${mm}31`).valid).toBe(true);
    });

    it('day 30 in a 30-day month is fine', () => {
      expect(validateDate('20230630').valid).toBe(true);
    });
  });

  describe('February', () => {
    it.each(['20230230', '20240230'])('Feb 30 (%s)', (date) => {
      expect(validateDate(date)).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'NOT_OK', day: 'NOT_OK' },
        message: 'Date:Cannot have 30 days in this month.',
      });
    });

    it.each(['20240229', '19960229', '20000229'])('Feb 29 in leap year %s', (date) => {
      expect(validateDate(date).valid).toBe(true);
    });

    it.each(['20230229', '19990229', '19000229'])('Feb 29 in non-leap year %s', (date) => {
      expect(validateDate(date)).toEqual({
        valid: false,
        flags: ALL_NOT_OK,
        message: 'Date:Not a leap year.Cannot have 29 days in this month.',
      });
    });

    it('century rule: 2000 (div by 400) is leap, 1900 is not', () => {
      expect(validateDate('20000229').valid).toBe(true);
      expect(validateDate('19000229').valid).toBe(false);
    });

    it('Feb 28 is always fine', () => {
      expect(validateDate('20230228').valid).toBe(true);
    });
  });

  describe('COBOL control-flow quirks', () => {
    it.each(['2023  31', '2023AB31'])('day 31 with blank/non-numeric month %s flags both', (date) => {
      expect(validateDate(date).flags).toEqual({ year: 'VALID', month: 'NOT_OK', day: 'NOT_OK' });
    });

    it('all field edits run; only the first message is kept', () => {
      expect(validateDate('18991345')).toEqual({
        valid: false,
        flags: ALL_NOT_OK,
        message: 'Date : Century is not valid.',
      });
    });

    it('cross-field check still runs after a field error', () => {
      // Month 13 fails EDIT-MONTH, then "not a 31-day month AND day 31" flags the day too.
      expect(validateDate('20231331')).toEqual({
        valid: false,
        flags: { year: 'VALID', month: 'NOT_OK', day: 'NOT_OK' },
        message: 'Date: Month must be a number between 1 and 12.',
      });
    });
  });

  describe('field name (WS-EDIT-VARIABLE-NAME)', () => {
    it('prefixes the trimmed name', () => {
      expect(validateDate('20230431', { fieldName: '  Open Date  ' }).message).toBe(
        'Open Date:Cannot have 31 days in this month.',
      );
    });

    it('name is limited to PIC X(25) and message to PIC X(75)', () => {
      const name = 'N'.repeat(30);
      const { message } = validateDate('20230229', { fieldName: name });
      expect(message).toBe(('N'.repeat(25) + ':Not a leap year.Cannot have 29 days in this month.').slice(0, 75));
      expect(message).toHaveLength(75);
    });
  });
});

describe('validateDateOfBirth', () => {
  const today = new Date(2024, 5, 15); // 2024-06-15 local

  it('past date is valid', () => {
    expect(validateDateOfBirth('19850320', { today })).toEqual({ valid: true, flags: ALL_VALID, message: '' });
  });

  it('yesterday is valid', () => {
    expect(validateDateOfBirth('20240614', { today }).valid).toBe(true);
  });

  it('today is rejected (current date must be strictly greater)', () => {
    expect(validateDateOfBirth('20240615', { today })).toEqual({
      valid: false,
      flags: ALL_NOT_OK,
      message: 'Date of Birth:cannot be in the future',
    });
  });

  it.each(['20240616', '20250101', '20991231'])('future date %s is rejected', (date) => {
    expect(validateDateOfBirth(date, { today })).toEqual({
      valid: false,
      flags: ALL_NOT_OK,
      message: 'Date of Birth:cannot be in the future',
    });
  });

  it('defaults to the real current date', () => {
    expect(validateDateOfBirth('20991231').valid).toBe(false);
    expect(validateDateOfBirth('19900101').valid).toBe(true);
  });

  it('date edit errors win; the future check is skipped', () => {
    expect(validateDateOfBirth('20990230', { today })).toEqual({
      valid: false,
      flags: { year: 'VALID', month: 'NOT_OK', day: 'NOT_OK' },
      message: 'Date of Birth:Cannot have 30 days in this month.',
    });
  });

  it('blank DOB', () => {
    expect(validateDateOfBirth('', { today }).message).toBe('Date of Birth : Year must be supplied.');
  });

  it('custom field name', () => {
    expect(validateDateOfBirth('20991231', { today, fieldName: 'DOB' }).message).toBe(
      'DOB:cannot be in the future',
    );
  });
});

describe('checkCalendarDate (CSUTLDTC / CEEDAYS replacement)', () => {
  it.each(['20240229', '20000229', '20231231', '19000101'])('%s is a real date', (date) => {
    expect(checkCalendarDate(date)).toEqual({ severity: 0, msgNo: 0, result: 'Date is valid' });
  });

  it.each(['20230229', '19000229', '20230431', '20231301', '20230100', '20230230'])(
    '%s fails the Date round-trip',
    (date) => {
      expect(checkCalendarDate(date)).toEqual({ severity: 3, msgNo: 2508, result: 'Datevalue error' });
    },
  );

  it('non-numeric input', () => {
    expect(checkCalendarDate('2023AB01')).toEqual({ severity: 3, msgNo: 2520, result: 'Nonnumeric data' });
  });

  it.each(['15821015', '99991231'])('%s is inside the Lilian range', (date) => {
    expect(checkCalendarDate(date).result).toBe('Date is valid');
  });

  it.each(['15821014', '00010101', '00000101'])('%s is before the Lilian range', (date) => {
    expect(checkCalendarDate(date)).toEqual({ severity: 3, msgNo: 2513, result: 'Unsupp. Range' });
  });
});
