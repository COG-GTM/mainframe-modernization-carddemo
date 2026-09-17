import { describe, expect, it } from 'vitest';
import { editDateCcyymmdd, editDateOfBirth } from '../src/common/dateValidation';
import { isValidStateZipCombo, isValidUsStateCode } from '../src/models/lookupCodes';

describe('CSUTLDPY date edits', () => {
  it('accepts a valid date', () => {
    expect(editDateCcyymmdd('20240229', 'Date').valid).toBe(true);
  });

  it('rejects a non leap year 29 February', () => {
    const result = editDateCcyymmdd('20230229', 'Date');
    expect(result.valid).toBe(false);
    expect(result.returnMsg).toContain('Not a leap year');
  });

  it('rejects an invalid month and an invalid day', () => {
    expect(editDateCcyymmdd('20241301', 'Date').returnMsg).toContain('between 1 and 12');
    expect(editDateCcyymmdd('20240431', 'Date').returnMsg).toContain('31 days');
    expect(editDateCcyymmdd('20240230', 'Date').returnMsg).toContain('30 days');
  });

  it('rejects a future date of birth', () => {
    const result = editDateOfBirth('20300101', 'DOB', new Date(2024, 0, 1));
    expect(result.valid).toBe(false);
    expect(result.returnMsg).toContain('cannot be in the future');
  });
});

describe('CSLKPCDY lookups', () => {
  it('validates state codes and state/zip combinations', () => {
    expect(isValidUsStateCode('NY')).toBe(true);
    expect(isValidUsStateCode('ZZ')).toBe(false);
    expect(isValidStateZipCombo('NY', '10001')).toBe(true);
    expect(isValidStateZipCombo('NY', '99001')).toBe(false);
  });
});
