import { Decimal } from 'decimal.js';

/** Monetary fields cross the wire as strings so no precision is lost. */
export function serializeRecord<T extends object>(record: T): Record<string, string> {
  const out: Record<string, string> = {};
  for (const [key, value] of Object.entries(record)) {
    out[key] = value instanceof Decimal ? value.toFixed(2) : String(value);
  }
  return out;
}
