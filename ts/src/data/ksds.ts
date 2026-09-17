import { RecordLayout } from '../models/layout';
import { DuplicateRecordError, EndOfFileError, RecordNotFoundError } from './errors';

/**
 * In-memory replacement for a VSAM KSDS.
 *
 * Records are held keyed by the same primary key the CICS FILE definition uses,
 * kept in ascending key order so that browses (STARTBR/READNEXT/READPREV) see
 * the same sequence as the mainframe. Alternate indexes (AIX paths such as
 * CARDAIX or CXACAIX) are declared per file and return records in key order.
 */
export interface KsdsOptions<T> {
  /** DDname/file name as coded in the CICS FILE definition, e.g. 'ACCTDAT'. */
  readonly name: string;
  readonly layout: RecordLayout<T>;
  /** Primary key, formatted exactly as stored in the record. */
  readonly keyOf: (record: T) => string;
  /** Alternate index paths, keyed by AIX file name. */
  readonly alternateKeys?: Readonly<Record<string, (record: T) => string>>;
}

export class Browse<T> {
  private position: number;

  constructor(
    private readonly keys: readonly string[],
    private readonly records: ReadonlyMap<string, T>,
    private readonly fileName: string,
    startKey?: string,
  ) {
    this.position = startKey === undefined ? 0 : lowerBound(keys, startKey);
  }

  /** READNEXT — throws EndOfFileError once the browse is exhausted. */
  readNext(): T {
    const record = this.tryReadNext();
    if (record === undefined) {
      throw new EndOfFileError(this.fileName);
    }
    return record;
  }

  tryReadNext(): T | undefined {
    if (this.position >= this.keys.length) {
      return undefined;
    }
    const key = this.keys[this.position] as string;
    this.position += 1;
    return this.records.get(key);
  }

  /** READPREV — throws EndOfFileError at the start of the file. */
  readPrev(): T {
    const record = this.tryReadPrev();
    if (record === undefined) {
      throw new EndOfFileError(this.fileName);
    }
    return record;
  }

  tryReadPrev(): T | undefined {
    if (this.position <= 0) {
      return undefined;
    }
    this.position -= 1;
    const key = this.keys[this.position] as string;
    return this.records.get(key);
  }
}

function lowerBound(keys: readonly string[], key: string): number {
  let low = 0;
  let high = keys.length;
  while (low < high) {
    const mid = (low + high) >>> 1;
    if ((keys[mid] as string) < key) {
      low = mid + 1;
    } else {
      high = mid;
    }
  }
  return low;
}

export class KsdsFile<T> {
  readonly name: string;
  readonly layout: RecordLayout<T>;
  private readonly keyOf: (record: T) => string;
  private readonly alternateKeys: Readonly<Record<string, (record: T) => string>>;
  private readonly records = new Map<string, T>();
  private sortedKeysCache: string[] | undefined;

  constructor(options: KsdsOptions<T>) {
    this.name = options.name;
    this.layout = options.layout;
    this.keyOf = options.keyOf;
    this.alternateKeys = options.alternateKeys ?? {};
  }

  get size(): number {
    return this.records.size;
  }

  private get sortedKeys(): string[] {
    if (this.sortedKeysCache === undefined) {
      this.sortedKeysCache = [...this.records.keys()].sort();
    }
    return this.sortedKeysCache;
  }

  private invalidate(): void {
    this.sortedKeysCache = undefined;
  }

  /** Replaces the whole file, as the IDCAMS REPRO load jobs do. */
  load(records: Iterable<T>): void {
    this.records.clear();
    for (const record of records) {
      this.records.set(this.keyOf(record), record);
    }
    this.invalidate();
  }

  /** READ — throws RecordNotFoundError (DFHRESP NOTFND). */
  read(key: string): T {
    const record = this.records.get(key);
    if (record === undefined) {
      throw new RecordNotFoundError(this.name, key);
    }
    return record;
  }

  tryRead(key: string): T | undefined {
    return this.records.get(key);
  }

  exists(key: string): boolean {
    return this.records.has(key);
  }

  /** All records in ascending primary key order. */
  readAll(): T[] {
    return this.sortedKeys.map((key) => this.records.get(key) as T);
  }

  /** STARTBR: positions the browse at the first key >= startKey. */
  startBrowse(startKey?: string): Browse<T> {
    return new Browse(this.sortedKeys, this.records, this.name, startKey);
  }

  /** WRITE — throws DuplicateRecordError (DFHRESP DUPREC). */
  write(record: T): void {
    const key = this.keyOf(record);
    if (this.records.has(key)) {
      throw new DuplicateRecordError(this.name, key);
    }
    this.records.set(key, record);
    this.invalidate();
  }

  /** REWRITE — throws RecordNotFoundError when the record is absent. */
  rewrite(record: T): void {
    const key = this.keyOf(record);
    if (!this.records.has(key)) {
      throw new RecordNotFoundError(this.name, key);
    }
    this.records.set(key, record);
  }

  /** WRITE or REWRITE depending on whether the key already exists. */
  put(record: T): void {
    const key = this.keyOf(record);
    if (!this.records.has(key)) {
      this.invalidate();
    }
    this.records.set(key, record);
  }

  /** DELETE — throws RecordNotFoundError when the record is absent. */
  delete(key: string): void {
    if (!this.records.delete(key)) {
      throw new RecordNotFoundError(this.name, key);
    }
    this.invalidate();
  }

  /** Reads through an alternate index path, in primary key order. */
  readByAlternate(indexName: string, key: string): T[] {
    const extract = this.alternateKeys[indexName];
    if (extract === undefined) {
      throw new Error(`file ${this.name} has no alternate index '${indexName}'`);
    }
    return this.readAll().filter((record) => extract(record) === key);
  }
}
