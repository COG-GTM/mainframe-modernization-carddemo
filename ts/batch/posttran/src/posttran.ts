#!/usr/bin/env node
/**
 * POSTTRAN job – STEP15 EXEC PGM=CBTRN02C (app/jcl/POSTTRAN.jcl).
 *
 * Binds the step's DD names to flat files and runs the CBTRN02C port.
 *
 *   DD name   JCL dataset                              default (flat file)
 *   DALYTRAN  AWS.M2.CARDDEMO.DALYTRAN.PS              <data-dir>/dailytran.txt
 *   TRANFILE  AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS       <data-dir>/transact.txt (empty if absent) -> <out-dir>/transact.txt
 *   XREFFILE  AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS       <data-dir>/cardxref.txt
 *   DALYREJS  AWS.M2.CARDDEMO.DALYREJS(+1)             <out-dir>/dalyrejs.txt
 *   ACCTFILE  AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS       <data-dir>/acctdata.txt -> <out-dir>/acctdata.txt
 *   TCATBALF  AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS       <data-dir>/tcatbal.txt  -> <out-dir>/tcatbal.txt
 *
 * Any DD can be overridden with `--dd NAME=path` or env `DD_NAME=path`; the
 * output side of a KSDS with `--dd NAME_OUT=path` / `DD_NAME_OUT`. Paths
 * ending in `.json` are read/written as JSON. `--in-place` writes the KSDS
 * updates back to their input files, like the real VSAM clusters.
 *
 * Exit code: CBTRN02C RETURN-CODE (0, or 4 if any transaction was rejected);
 * 12 if the program abended (U0999) – outputs are not written in that case.
 */
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { AbendError, REJECT_RECORD_LAYOUT, runCbtrn02c, type PostTranFiles } from './cbtrn02c/index.js';
import {
  ACCOUNT_LAYOUT,
  accountKey,
  CARD_XREF_LAYOUT,
  cardXrefKey,
  DAILY_TRAN_LAYOUT,
  TRAN_CAT_BAL_LAYOUT,
  TRAN_LAYOUT,
  tranCatBalKey,
  tranKey,
} from './copybooks/index.js';
import { FlatFileKeyedFile, FlatFileSequentialInput, FlatFileSequentialOutput } from './dal/index.js';

export const ABEND_EXIT_CODE = 12;

const PROJECT_DIR = fileURLToPath(new URL('..', import.meta.url));
export const DEFAULT_DATA_DIR = resolve(PROJECT_DIR, '../../../app/data/ASCII');
export const DEFAULT_OUT_DIR = resolve(PROJECT_DIR, 'out');

export interface DdBindings {
  DALYTRAN: string;
  TRANFILE: string;
  TRANFILE_OUT: string;
  XREFFILE: string;
  DALYREJS: string;
  ACCTFILE: string;
  ACCTFILE_OUT: string;
  TCATBALF: string;
  TCATBALF_OUT: string;
}

export interface JobConfig {
  dataDir?: string;
  outDir?: string;
  inPlace?: boolean;
  overrides?: Partial<Record<keyof DdBindings, string>>;
}

export function resolveDdBindings(config: JobConfig = {}): DdBindings {
  const data = resolve(config.dataDir ?? DEFAULT_DATA_DIR);
  const out = resolve(config.outDir ?? DEFAULT_OUT_DIR);
  const o = config.overrides ?? {};
  const pick = (name: keyof DdBindings, fallback: string): string => resolve(o[name] ?? fallback);
  const dd = {
    DALYTRAN: pick('DALYTRAN', `${data}/dailytran.txt`),
    TRANFILE: pick('TRANFILE', `${data}/transact.txt`),
    XREFFILE: pick('XREFFILE', `${data}/cardxref.txt`),
    DALYREJS: pick('DALYREJS', `${out}/dalyrejs.txt`),
    ACCTFILE: pick('ACCTFILE', `${data}/acctdata.txt`),
    TCATBALF: pick('TCATBALF', `${data}/tcatbal.txt`),
  };
  return {
    ...dd,
    TRANFILE_OUT: pick('TRANFILE_OUT', config.inPlace ? dd.TRANFILE : `${out}/transact.txt`),
    ACCTFILE_OUT: pick('ACCTFILE_OUT', config.inPlace ? dd.ACCTFILE : `${out}/acctdata.txt`),
    TCATBALF_OUT: pick('TCATBALF_OUT', config.inPlace ? dd.TCATBALF : `${out}/tcatbal.txt`),
  };
}

export function flatFileBindings(dd: DdBindings): PostTranFiles {
  return {
    dalytran: new FlatFileSequentialInput(dd.DALYTRAN, DAILY_TRAN_LAYOUT),
    tranfile: new FlatFileKeyedFile(dd.TRANFILE, TRAN_LAYOUT, tranKey, {
      outputPath: dd.TRANFILE_OUT,
      createIfMissing: true,
    }),
    xreffile: new FlatFileKeyedFile(dd.XREFFILE, CARD_XREF_LAYOUT, cardXrefKey, { readOnly: true }),
    dalyrejs: new FlatFileSequentialOutput(dd.DALYREJS, REJECT_RECORD_LAYOUT),
    acctfile: new FlatFileKeyedFile(dd.ACCTFILE, ACCOUNT_LAYOUT, accountKey, { outputPath: dd.ACCTFILE_OUT }),
    tcatbalf: new FlatFileKeyedFile(dd.TCATBALF, TRAN_CAT_BAL_LAYOUT, tranCatBalKey, { outputPath: dd.TCATBALF_OUT }),
  };
}

const DD_NAMES: readonly (keyof DdBindings)[] = [
  'DALYTRAN',
  'TRANFILE',
  'TRANFILE_OUT',
  'XREFFILE',
  'DALYREJS',
  'ACCTFILE',
  'ACCTFILE_OUT',
  'TCATBALF',
  'TCATBALF_OUT',
];

function usage(): string {
  return [
    'Usage: npm run posttran -- [options]',
    '',
    '  --data-dir <dir>   input datasets (default: app/data/ASCII)',
    '  --out-dir <dir>    output datasets (default: ts/batch/posttran/out)',
    '  --dd NAME=path     override a DD (DALYTRAN, TRANFILE[_OUT], XREFFILE, DALYREJS,',
    '                     ACCTFILE[_OUT], TCATBALF[_OUT]); also env DD_NAME',
    '  --in-place         write KSDS updates back to their input files',
    '  -h, --help',
  ].join('\n');
}

export async function main(argv: string[] = process.argv.slice(2), env: NodeJS.ProcessEnv = process.env): Promise<number> {
  const { values } = parseArgs({
    args: argv,
    options: {
      'data-dir': { type: 'string' },
      'out-dir': { type: 'string' },
      dd: { type: 'string', multiple: true },
      'in-place': { type: 'boolean', default: false },
      help: { type: 'boolean', short: 'h', default: false },
    },
    strict: true,
  });
  if (values.help) {
    console.log(usage());
    return 0;
  }

  const overrides: Partial<Record<keyof DdBindings, string>> = {};
  for (const name of DD_NAMES) {
    const fromEnv = env[`DD_${name}`];
    if (fromEnv) overrides[name] = fromEnv;
  }
  for (const spec of values.dd ?? []) {
    const eq = spec.indexOf('=');
    const name = spec.slice(0, eq).toUpperCase() as keyof DdBindings;
    if (eq <= 0 || !DD_NAMES.includes(name)) {
      console.error(`Unknown --dd "${spec}"\n\n${usage()}`);
      return 16;
    }
    overrides[name] = spec.slice(eq + 1);
  }

  const dd = resolveDdBindings({
    dataDir: values['data-dir'],
    outDir: values['out-dir'],
    inPlace: values['in-place'],
    overrides,
  });

  try {
    const result = await runCbtrn02c(flatFileBindings(dd));
    return result.returnCode;
  } catch (err) {
    if (err instanceof AbendError) {
      console.error(err.message);
      return ABEND_EXIT_CODE;
    }
    throw err;
  }
}

const invokedDirectly = process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url);
if (invokedDirectly) {
  main().then(
    (rc) => {
      process.exitCode = rc;
    },
    (err: unknown) => {
      console.error(err);
      process.exitCode = ABEND_EXIT_CODE;
    },
  );
}
