/**
 * Parser for COBOL PICTURE clauses used by the copybooks in app/cpy/.
 *
 * Supports the subset present in CardDemo: PIC X(n), PIC 9(n), PIC S9(n)V9(m),
 * with optional USAGE COMP-3 (packed decimal), plus numeric-edited pictures
 * (e.g. -ZZZ,ZZZ,ZZZ.ZZ) which are treated as fixed-length display fields.
 */

export type PictureKind = 'alphanumeric' | 'numeric' | 'numeric-edited';

export interface PictureSpec {
  /** Original PIC text, e.g. "S9(10)V99". */
  readonly pic: string;
  readonly kind: PictureKind;
  /** Number of digit positions (numeric only). */
  readonly digits: number;
  /** Number of digits after the implied decimal point (V). */
  readonly scale: number;
  readonly signed: boolean;
  /** COMP-3 packed decimal storage. */
  readonly packed: boolean;
  /** Storage length in bytes. */
  readonly length: number;
}

const EDIT_CHARS = new Set(['Z', ',', '.', '+', '-', '*', '$', 'B', '/', 'C', 'R', 'D', 'B']);

/** Expands `9(04)` style repeat factors into a flat character list, e.g. "9999". */
function expand(pic: string): string {
  let out = '';
  let i = 0;
  while (i < pic.length) {
    const ch = pic[i] as string;
    if (pic[i + 1] === '(') {
      const close = pic.indexOf(')', i + 2);
      if (close === -1) throw new SyntaxError(`unbalanced repeat factor in PIC ${pic}`);
      const count = Number.parseInt(pic.slice(i + 2, close), 10);
      if (!Number.isInteger(count) || count < 1) {
        throw new SyntaxError(`invalid repeat factor in PIC ${pic}`);
      }
      out += ch.repeat(count);
      i = close + 1;
    } else {
      out += ch;
      i += 1;
    }
  }
  return out;
}

/** Packed-decimal (COMP-3) byte length: one nibble per digit plus a sign nibble. */
export function packedLength(digits: number): number {
  return Math.floor(digits / 2) + 1;
}

export function parsePicture(picText: string, usage?: string): PictureSpec {
  const pic = picText.trim().toUpperCase().replace(/\.$/, '');
  const packed = /COMP-3|COMPUTATIONAL-3/.test((usage ?? '').toUpperCase());
  const chars = expand(pic);

  if (/^[AX]+$/.test(chars)) {
    return {
      pic,
      kind: 'alphanumeric',
      digits: 0,
      scale: 0,
      signed: false,
      packed: false,
      length: chars.length,
    };
  }

  if (/^S?9*V?9*$/.test(chars) && chars.includes('9')) {
    const signed = chars.startsWith('S');
    const body = signed ? chars.slice(1) : chars;
    const [intPart = '', decPart = ''] = body.split('V');
    const digits = intPart.length + decPart.length;
    return {
      pic,
      kind: 'numeric',
      digits,
      scale: decPart.length,
      signed,
      packed,
      length: packed ? packedLength(digits) : digits,
    };
  }

  if ([...chars].every((ch) => EDIT_CHARS.has(ch) || ch === '9' || ch === '0')) {
    return {
      pic,
      kind: 'numeric-edited',
      digits: [...chars].filter((ch) => ch === '9' || ch === 'Z' || ch === '*').length,
      scale: (chars.split('.')[1] ?? '').replace(/[^9Z*]/g, '').length,
      signed: chars.includes('-') || chars.includes('+'),
      packed: false,
      length: chars.length,
    };
  }

  throw new SyntaxError(`unsupported PICTURE clause: ${picText}`);
}
