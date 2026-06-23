import { describe, it, expect } from 'vitest';
import { Money } from '../src/money';

describe('Money (fixed-point decimal, COBOL S9(n)V99 equivalent)', () => {
  it('does exact decimal arithmetic without float drift', () => {
    // 0.1 + 0.2 must be exactly 0.30, not 0.30000000000000004
    expect(Money.fromDecimal(0.1).add(Money.fromDecimal(0.2)).toString()).toBe('0.30');
  });

  it('subtracts to exactly zero', () => {
    const bal = Money.fromDecimal(1234.56);
    expect(bal.subtract(bal).toString()).toBe('0.00');
  });

  it('formats with two decimal places', () => {
    expect(Money.fromDecimal(1000).toString()).toBe('1000.00');
    expect(Money.fromCents(5).toString()).toBe('0.05');
  });

  it('detects non-positive balances', () => {
    expect(Money.zero().isZeroOrLess()).toBe(true);
    expect(Money.fromCents(-100).isZeroOrLess()).toBe(true);
    expect(Money.fromDecimal(0.01).isPositive()).toBe(true);
  });
});
