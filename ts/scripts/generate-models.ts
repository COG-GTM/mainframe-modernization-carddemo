/**
 * Generates the TypeScript record models in src/models/ from the COBOL
 * copybooks in app/cpy/.
 *
 * Run with `npm run generate:models`. The generated files are committed so the
 * models can be reviewed alongside the copybooks they mirror.
 */

import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

import { parsePicture, type PictureSpec } from '../src/cobol/picture.js';

const HERE = dirname(fileURLToPath(import.meta.url));
const TS_ROOT = join(HERE, '..');
const REPO_ROOT = join(TS_ROOT, '..');
const MODELS_DIR = join(TS_ROOT, 'src', 'models');

interface CopybookTarget {
  readonly copybook: string;
  readonly outputFile: string;
  readonly interfaceName: string;
  readonly specName: string;
  /** COBOL 01-level name to extract; defaults to the first 01 level found. */
  readonly level01?: string;
}

const TARGETS: readonly CopybookTarget[] = [
  {
    copybook: 'app/cpy/CVACT01Y.cpy',
    outputFile: 'account.ts',
    interfaceName: 'AccountRecord',
    specName: 'ACCOUNT_RECORD',
  },
  {
    copybook: 'app/cpy/CVACT02Y.cpy',
    outputFile: 'card.ts',
    interfaceName: 'CardRecord',
    specName: 'CARD_RECORD',
  },
  {
    copybook: 'app/cpy/CVACT03Y.cpy',
    outputFile: 'card-xref.ts',
    interfaceName: 'CardXrefRecord',
    specName: 'CARD_XREF_RECORD',
  },
  {
    copybook: 'app/cpy/CVCUS01Y.cpy',
    outputFile: 'customer.ts',
    interfaceName: 'CustomerRecord',
    specName: 'CUSTOMER_RECORD',
  },
  {
    copybook: 'app/cpy/CVTRA01Y.cpy',
    outputFile: 'tran-cat-balance.ts',
    interfaceName: 'TranCatBalanceRecord',
    specName: 'TRAN_CAT_BALANCE_RECORD',
  },
  {
    copybook: 'app/cpy/CVTRA02Y.cpy',
    outputFile: 'disclosure-group.ts',
    interfaceName: 'DisclosureGroupRecord',
    specName: 'DISCLOSURE_GROUP_RECORD',
  },
  {
    copybook: 'app/cpy/CVTRA03Y.cpy',
    outputFile: 'tran-type.ts',
    interfaceName: 'TranTypeRecord',
    specName: 'TRAN_TYPE_RECORD',
  },
  {
    copybook: 'app/cpy/CVTRA04Y.cpy',
    outputFile: 'tran-category.ts',
    interfaceName: 'TranCategoryRecord',
    specName: 'TRAN_CATEGORY_RECORD',
  },
  {
    copybook: 'app/cpy/CVTRA05Y.cpy',
    outputFile: 'transaction.ts',
    interfaceName: 'TransactionRecord',
    specName: 'TRANSACTION_RECORD',
  },
  {
    copybook: 'app/cpy/CVTRA06Y.cpy',
    outputFile: 'daily-transaction.ts',
    interfaceName: 'DailyTransactionRecord',
    specName: 'DAILY_TRANSACTION_RECORD',
  },
  {
    copybook: 'app/cpy/CSUSR01Y.cpy',
    outputFile: 'security-user.ts',
    interfaceName: 'SecurityUserRecord',
    specName: 'SECURITY_USER_RECORD',
  },
];

interface ParsedField {
  readonly cobolName: string;
  readonly name: string;
  readonly offset: number;
  readonly length: number;
  readonly picture: PictureSpec;
  readonly filler: boolean;
  readonly group: string | undefined;
}

function toCamelCase(cobolName: string): string {
  return cobolName
    .toLowerCase()
    .split('-')
    .filter((part) => part !== '')
    .map((part, index) => (index === 0 ? part : part[0]?.toUpperCase() + part.slice(1)))
    .join('');
}

/** Strips the sequence number area (cols 1-6) and the identification area (cols 73+). */
function sourceLines(content: string): string[] {
  return content
    .split(/\r?\n/)
    .map((line) => line.replace(/\t/g, '    '))
    .map((line) => (line.length > 72 ? line.slice(0, 72) : line))
    .map((line) => (line.length > 6 ? line.slice(6) : ''))
    .filter((line) => line.trim() !== '' && !line.startsWith('*') && !line.trim().startsWith('*'));
}

function parseCopybook(target: CopybookTarget): { fields: ParsedField[]; recordLength: number } {
  const content = readFileSync(join(REPO_ROOT, target.copybook), 'latin1');
  const fields: ParsedField[] = [];
  const groupStack: { level: number; name: string }[] = [];
  let offset = 0;
  let inTargetRecord = false;
  let fillerCount = 0;

  for (const line of sourceLines(content)) {
    const text = line.trim().replace(/\s+/g, ' ');
    const match = /^(\d{2})\s+([A-Z0-9-]+)(.*)$/.exec(text);
    if (match === null) continue;

    const level = Number(match[1]);
    const cobolName = match[2] as string;
    const rest = (match[3] ?? '').trim();

    if (level === 88) continue;
    if (/\bREDEFINES\b/.test(rest)) continue;

    if (level === 1) {
      if (inTargetRecord) break;
      inTargetRecord = target.level01 === undefined || target.level01 === cobolName;
      groupStack.length = 0;
      continue;
    }
    if (!inTargetRecord) continue;

    while (groupStack.length > 0 && (groupStack[groupStack.length - 1] as { level: number }).level >= level) {
      groupStack.pop();
    }

    const picMatch = /PIC(?:TURE)?\s+([^\s.]+)/.exec(rest);
    if (picMatch === null) {
      groupStack.push({ level, name: cobolName });
      continue;
    }

    const usageMatch = /\b(COMP-3|COMPUTATIONAL-3|COMP|BINARY)\b/.exec(rest);
    const picture = parsePicture(picMatch[1] as string, usageMatch?.[1]);
    const filler = cobolName === 'FILLER';
    if (filler) fillerCount += 1;

    fields.push({
      cobolName,
      name: filler ? `filler${fillerCount}` : toCamelCase(cobolName),
      offset,
      length: picture.length,
      picture,
      filler,
      group: (groupStack[groupStack.length - 1] as { name: string } | undefined)?.name,
    });
    offset += picture.length;
  }

  if (fields.length === 0) throw new Error(`no fields parsed from ${target.copybook}`);
  return { fields, recordLength: offset };
}

function tsType(picture: PictureSpec): string {
  return picture.kind === 'numeric' ? 'number' : 'string';
}

function fieldDoc(field: ParsedField): string {
  const parts = [`${field.cobolName} PIC ${field.picture.pic}`];
  if (field.picture.packed) parts.push('COMP-3');
  if (field.group !== undefined) parts.push(`in ${field.group}`);
  parts.push(`bytes ${field.offset + 1}-${field.offset + field.length}`);
  return parts.join(', ');
}

function renderModel(target: CopybookTarget, fields: ParsedField[], recordLength: number): string {
  const dataFields = fields.filter((field) => !field.filler);

  const interfaceBody = dataFields
    .map((field) => `  /** ${fieldDoc(field)} */\n  ${field.name}: ${tsType(field.picture)};`)
    .join('\n');

  const specFields = fields
    .map((field) => {
      const picture = field.picture;
      return [
        '    {',
        `      name: '${field.name}',`,
        `      cobolName: '${field.cobolName}',`,
        `      offset: ${field.offset},`,
        `      length: ${field.length},`,
        `      filler: ${field.filler},`,
        '      picture: {',
        `        pic: '${picture.pic}',`,
        `        kind: '${picture.kind}',`,
        `        digits: ${picture.digits},`,
        `        scale: ${picture.scale},`,
        `        signed: ${picture.signed},`,
        `        packed: ${picture.packed},`,
        `        length: ${picture.length},`,
        '      },',
        '    },',
      ].join('\n');
    })
    .join('\n');

  return `/**
 * Generated from ${target.copybook} by ts/scripts/generate-models.ts.
 * Do not edit by hand; re-run \`npm run generate:models\` instead.
 *
 * Record length: ${recordLength} bytes.
 */

import type { RecordSpec } from '../cobol/record.js';

export interface ${target.interfaceName} {
${interfaceBody}
}

export const ${target.specName}_LENGTH = ${recordLength};

export const ${target.specName}_SPEC: RecordSpec<${target.interfaceName}> = {
  name: '${target.interfaceName}',
  copybook: '${target.copybook}',
  recordLength: ${recordLength},
  fields: [
${specFields}
  ],
};
`;
}

function main(): void {
  mkdirSync(MODELS_DIR, { recursive: true });
  const exports: string[] = [];

  for (const target of TARGETS) {
    const { fields, recordLength } = parseCopybook(target);
    writeFileSync(join(MODELS_DIR, target.outputFile), renderModel(target, fields, recordLength));
    exports.push(`export * from './${target.outputFile.replace(/\.ts$/, '.js')}';`);
    process.stdout.write(`${target.copybook} -> src/models/${target.outputFile} (${recordLength} bytes)\n`);
  }

  exports.push(`export * from './commarea.js';`);
  writeFileSync(
    join(MODELS_DIR, 'index.ts'),
    `/** Generated by ts/scripts/generate-models.ts. */\n\n${exports.join('\n')}\n`,
  );
}

main();
