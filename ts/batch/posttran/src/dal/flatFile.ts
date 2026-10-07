import { existsSync } from 'node:fs';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, extname } from 'node:path';
import type { RecordLayout } from '../copybooks/layout.js';
import { InMemoryKeyedFile } from './memory.js';
import type { FileStatus, ReadResult, SequentialInputFile, SequentialOutputFile } from './types.js';

/**
 * Flat-file implementations of the data-access interfaces.
 *
 * Format is chosen by file extension:
 *  - `.json` – JSON array of records (amounts as decimal strings)
 *  - anything else – fixed-width ASCII, one record per line, laid out exactly
 *    as the copybook (the format of app/data/ASCII/*.txt)
 */
export type FlatFileFormat = 'fixed' | 'json';

export function formatFor(path: string): FlatFileFormat {
  return extname(path).toLowerCase() === '.json' ? 'json' : 'fixed';
}

export async function readRecords<T>(path: string, layout: RecordLayout<T>): Promise<T[]> {
  const text = await readFile(path, 'utf8');
  if (formatFor(path) === 'json') {
    const arr = JSON.parse(text) as Record<string, unknown>[];
    if (!Array.isArray(arr)) throw new Error(`${path}: expected a JSON array`);
    return arr.map((o) => layout.fromJSON(o));
  }
  return text
    .split(/\r?\n/)
    .filter((line) => line.length > 0)
    .map((line) => layout.parse(line));
}

export async function writeRecords<T>(path: string, layout: RecordLayout<T>, records: readonly T[]): Promise<void> {
  await mkdir(dirname(path), { recursive: true });
  const body =
    formatFor(path) === 'json'
      ? JSON.stringify(records.map((r) => layout.toJSON(r)), null, 2) + '\n'
      : records.map((r) => layout.format(r) + '\n').join('');
  await writeFile(path, body, 'utf8');
}

export class FlatFileSequentialInput<T> implements SequentialInputFile<T> {
  private records: T[] = [];
  private pos = 0;
  private isOpen = false;

  constructor(
    readonly path: string,
    private readonly layout: RecordLayout<T>,
  ) {}

  async open(): Promise<FileStatus> {
    if (!existsSync(this.path)) return '35';
    try {
      this.records = await readRecords(this.path, this.layout);
    } catch {
      return '30';
    }
    this.pos = 0;
    this.isOpen = true;
    return '00';
  }

  async read(): Promise<ReadResult<T>> {
    if (!this.isOpen) return { status: '48' };
    if (this.pos >= this.records.length) return { status: '10' };
    return { status: '00', record: this.records[this.pos++] as T };
  }

  async close(): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    this.isOpen = false;
    this.records = [];
    return '00';
  }
}

export class FlatFileSequentialOutput<T> implements SequentialOutputFile<T> {
  private records: T[] = [];
  private isOpen = false;

  constructor(
    readonly path: string,
    private readonly layout: RecordLayout<T>,
  ) {}

  async open(): Promise<FileStatus> {
    this.records = [];
    this.isOpen = true;
    try {
      await writeRecords(this.path, this.layout, []);
    } catch {
      return '30';
    }
    return '00';
  }

  async write(record: T): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    this.records.push({ ...record });
    return '00';
  }

  async close(): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    this.isOpen = false;
    try {
      await writeRecords(this.path, this.layout, this.records);
    } catch {
      return '30';
    }
    return '00';
  }
}

export interface FlatFileKeyedOptions {
  /** Where updated contents are written on CLOSE (defaults to `inputPath`, i.e. update in place). */
  outputPath?: string;
  /** Start empty instead of returning '35' when `inputPath` does not exist (e.g. a new TRANSACT KSDS). */
  createIfMissing?: boolean;
  /** Opened INPUT: WRITE/REWRITE return '48' and nothing is written on CLOSE. */
  readOnly?: boolean;
}

/**
 * KSDS backed by a flat file: loaded into memory on OPEN, written back in key
 * order on CLOSE – the same order an IDCAMS REPRO unload of the cluster yields.
 */
export class FlatFileKeyedFile<T> extends InMemoryKeyedFile<T> {
  readonly outputPath: string;

  constructor(
    readonly inputPath: string,
    private readonly layout: RecordLayout<T>,
    keyOf: (record: T) => string,
    private readonly options: FlatFileKeyedOptions = {},
  ) {
    super(keyOf);
    this.outputPath = options.outputPath ?? inputPath;
  }

  override async open(): Promise<FileStatus> {
    this.data.clear();
    if (existsSync(this.inputPath)) {
      try {
        for (const r of await readRecords(this.inputPath, this.layout)) this.data.set(this.keyOf(r), r);
      } catch {
        return '30';
      }
    } else if (!this.options.createIfMissing) {
      return '35';
    }
    return super.open();
  }

  override async write(record: T): Promise<FileStatus> {
    return this.options.readOnly ? '48' : super.write(record);
  }

  override async rewrite(record: T): Promise<FileStatus> {
    return this.options.readOnly ? '48' : super.rewrite(record);
  }

  override async close(): Promise<FileStatus> {
    const status = await super.close();
    if (status !== '00' || this.options.readOnly) return status;
    try {
      await writeRecords(this.outputPath, this.layout, this.entries());
    } catch {
      return '30';
    }
    return '00';
  }
}
