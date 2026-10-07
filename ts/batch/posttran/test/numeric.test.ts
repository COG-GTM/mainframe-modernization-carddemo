import { describe, expect, it } from 'vitest';
import {
  compareAlphanumeric,
  dec,
  decodePacked,
  decodeZoned,
  encodePacked,
  encodeZoned,
  NumericDataError,
  toPic,
} from '../src/copybooks/numeric.js';

describe('zoned decimal (DISPLAY sign overpunch)', () => {
  it.each([
    ['0000005047G', '504.77'],
    ['0000009190}', '-919.00'],
    ['0000000678H', '67.88'],
    ['0000000000{', '0.00'],
    ['0000000123R', '-12.39'],
    ['00000001234', '12.34'],
  ])('decodes %s as %s', (raw, expected) => {
    expect(decodeZoned(raw, 9, 2).toFixed(2)).toBe(expected);
  });

  it.each([
    ['504.77', '0000005047G'],
    ['-919.00', '0000009190}'],
    ['0', '0000000000{'],
    ['-0.01', '0000000000J'],
  ])('encodes %s as %s', (value, expected) => {
    expect(encodeZoned(dec(value), 9, 2)).toBe(expected);
  });

  it('treats an all-space field as zero (INITIALIZE)', () => {
    expect(decodeZoned('           ', 9, 2).isZero()).toBe(true);
  });

  it('rejects bad sign characters and non-numeric data', () => {
    expect(() => decodeZoned('0000000000Z', 9, 2)).toThrow(NumericDataError);
    expect(() => decodeZoned('00000A0000{', 9, 2)).toThrow(NumericDataError);
  });
});

describe('toPic – COBOL store truncation', () => {
  it('truncates fractional digits without rounding', () => {
    expect(toPic(dec('1.239'), 9, 2).toFixed(2)).toBe('1.23');
    expect(toPic(dec('-1.239'), 9, 2).toFixed(2)).toBe('-1.23');
  });

  it('drops high-order digits on overflow (no ON SIZE ERROR)', () => {
    expect(toPic(dec('1234567890.12'), 9, 2).toFixed(2)).toBe('234567890.12');
    expect(toPic(dec('-1000000000.50'), 9, 2).toFixed(2)).toBe('-0.50');
  });
});

describe('COMP-3 packed decimal', () => {
  it('round-trips S9(09)V99', () => {
    for (const v of ['0', '504.77', '-919.00', '999999999.99', '-0.01']) {
      const packed = encodePacked(dec(v), 9, 2);
      expect(packed.length).toBe(6);
      expect(decodePacked(packed, 2).toFixed(2)).toBe(dec(v).toFixed(2));
    }
  });

  it('uses C/D sign nibbles', () => {
    expect([...encodePacked(dec('-12.34'), 3, 2)]).toEqual([0x01, 0x23, 0x4d]);
    expect([...encodePacked(dec('12.34'), 3, 2)]).toEqual([0x01, 0x23, 0x4c]);
  });
});

describe('compareAlphanumeric', () => {
  it('pads the shorter operand with spaces like COBOL', () => {
    expect(compareAlphanumeric('2025-05-20', '2022-06-10')).toBe(1);
    expect(compareAlphanumeric('2022-06-10', '2022-06-10')).toBe(0);
    expect(compareAlphanumeric('', '2022-06-10')).toBe(-1);
    expect(compareAlphanumeric('AB', 'AB  ')).toBe(0);
  });
});
