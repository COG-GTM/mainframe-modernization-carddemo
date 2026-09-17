/** Numeric edited PIC formatting used by the report and screen layouts. */

function groupedAmount(value: number): string {
  const cents = Math.round(Math.abs(value) * 100);
  const whole = Math.trunc(cents / 100);
  const fraction = String(cents % 100).padStart(2, '0');
  const grouped = String(whole).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  return `${grouped}.${fraction}`;
}

/** PIC -ZZZ,ZZZ,ZZZ.ZZ — 14 characters, blank sign when positive. */
export function formatPicSignedZ(value: number): string {
  const sign = value < 0 ? '-' : ' ';
  return (sign + groupedAmount(value)).padStart(14, ' ');
}

/** PIC +ZZZ,ZZZ,ZZZ.ZZ — 14 characters, explicit sign. */
export function formatPicPlusZ(value: number): string {
  const sign = value < 0 ? '-' : '+';
  return (sign + groupedAmount(value)).padStart(14, ' ');
}

/** PIC -99999999.99 style used on the BMS maps for amounts. */
export function formatSignedAmount(value: number, integerDigits: number): string {
  const sign = value < 0 ? '-' : ' ';
  const cents = Math.round(Math.abs(value) * 100);
  const whole = String(Math.trunc(cents / 100)).padStart(integerDigits, '0');
  const fraction = String(cents % 100).padStart(2, '0');
  return `${sign}${whole}.${fraction}`;
}
