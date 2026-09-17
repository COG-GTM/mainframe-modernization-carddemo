/**
 * Seeds the migrated datasets from the sample data shipped in app/data/.
 *
 * This is the TypeScript equivalent of running the load JCLs in app/jcl/ in the
 * documented order (DUSRSECJ, ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE,
 * DISCGRP, TCATBALF, TRANCATG, TRANTYPE). Records are copied byte for byte, as
 * IDCAMS REPRO does; EBCDIC input is transcoded to the ASCII code page first.
 */

import { existsSync, readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

import { decodeEbcdic } from '../cobol/ebcdic.js';
import { splitFixedLengthRecords, splitTextRecords } from '../cobol/record.js';
import type { KsdsName, SequentialName } from '../data/catalog.js';
import type { CardDemoStore } from '../data/store.js';
import {
  INLINE_TRANSACT_INIT_RECORD,
  INLINE_USRSEC_RECORDS,
  SEED_SOURCES,
  type SeedSource,
} from './sources.js';

const HERE = dirname(fileURLToPath(import.meta.url));
export const REPO_ROOT = join(HERE, '..', '..', '..');
export const ASCII_DATA_DIR = join(REPO_ROOT, 'app', 'data', 'ASCII');
export const EBCDIC_DATA_DIR = join(REPO_ROOT, 'app', 'data', 'EBCDIC');

export type SeedEncoding = 'ascii' | 'ebcdic';

export interface SeedOptions {
  readonly encoding?: SeedEncoding;
  readonly asciiDir?: string;
  readonly ebcdicDir?: string;
}

export interface SeedResult {
  readonly job: string;
  readonly dataset: string;
  readonly records: number;
  readonly source: string;
}

function readEbcdicFile(source: SeedSource, ebcdicDir: string): { raw: string[]; origin: string } | undefined {
  if (source.ebcdicFile === undefined) return undefined;
  const path = join(ebcdicDir, source.ebcdicFile);
  if (!existsSync(path)) return undefined;

  const decoded = decodeEbcdic(readFileSync(path));
  return {
    raw: splitFixedLengthRecords(decoded, source.recordLength).map((record) => record.toString('latin1')),
    origin: path,
  };
}

function readRecords(source: SeedSource, options: Required<SeedOptions>): { raw: string[]; origin: string } | undefined {
  if (options.encoding === 'ebcdic') {
    const loaded = readEbcdicFile(source, options.ebcdicDir);
    if (loaded !== undefined) return loaded;
  }

  if (source.asciiFile !== undefined) {
    const path = join(options.asciiDir, source.asciiFile);
    if (existsSync(path)) {
      return {
        raw: splitTextRecords(readFileSync(path), source.recordLength).map((record) =>
          record.toString('latin1'),
        ),
        origin: path,
      };
    }
  }

  const ebcdic = readEbcdicFile(source, options.ebcdicDir);
  if (ebcdic !== undefined) return ebcdic;

  if (source.dataset === 'USRSEC') {
    return { raw: [...INLINE_USRSEC_RECORDS], origin: 'app/jcl/DUSRSECJ.jcl (in-stream)' };
  }
  if (source.dataset === 'TRANSACT') {
    return { raw: [INLINE_TRANSACT_INIT_RECORD], origin: 'app/jcl/TRANFILE.jcl (DALYTRAN.PS.INIT)' };
  }

  return undefined;
}

/** Runs every load job, replacing the contents of each target dataset. */
export function seedAll(store: CardDemoStore, options: SeedOptions = {}): SeedResult[] {
  const resolved: Required<SeedOptions> = {
    encoding: options.encoding ?? 'ascii',
    asciiDir: options.asciiDir ?? ASCII_DATA_DIR,
    ebcdicDir: options.ebcdicDir ?? EBCDIC_DATA_DIR,
  };

  const results: SeedResult[] = [];
  for (const source of SEED_SOURCES) {
    const loaded = readRecords(source, resolved);
    if (loaded === undefined) continue;

    if (source.kind === 'ksds') {
      store.open(source.dataset as KsdsName).loadRaw(loaded.raw);
    } else {
      store.openSequential(source.dataset as SequentialName).loadRaw(loaded.raw);
    }

    results.push({
      job: source.job,
      dataset: source.dataset,
      records: loaded.raw.length,
      source: loaded.origin,
    });
  }

  return results;
}
