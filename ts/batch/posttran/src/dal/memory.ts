import type { FileStatus, KeyedFile, ReadResult, SequentialInputFile, SequentialOutputFile } from './types.js';

const clone = <T>(r: T): T => ({ ...r });

/** In-memory sequential input – useful for tests and for feeding records from other sources. */
export class InMemorySequentialInput<T> implements SequentialInputFile<T> {
  private pos = 0;
  private isOpen = false;
  constructor(private readonly records: readonly T[]) {}

  async open(): Promise<FileStatus> {
    this.pos = 0;
    this.isOpen = true;
    return '00';
  }

  async read(): Promise<ReadResult<T>> {
    if (!this.isOpen) return { status: '48' };
    if (this.pos >= this.records.length) return { status: '10' };
    return { status: '00', record: clone(this.records[this.pos++] as T) };
  }

  async close(): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    this.isOpen = false;
    return '00';
  }
}

export class InMemorySequentialOutput<T> implements SequentialOutputFile<T> {
  readonly records: T[] = [];
  private isOpen = false;

  async open(): Promise<FileStatus> {
    this.records.length = 0;
    this.isOpen = true;
    return '00';
  }

  async write(record: T): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    this.records.push(clone(record));
    return '00';
  }

  async close(): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    this.isOpen = false;
    return '00';
  }
}

/** In-memory KSDS keyed by `keyOf(record)`. `entries()` yields records in key order. */
export class InMemoryKeyedFile<T> implements KeyedFile<T> {
  protected readonly data = new Map<string, T>();
  protected isOpen = false;

  constructor(
    protected readonly keyOf: (record: T) => string,
    initial: readonly T[] = [],
  ) {
    for (const r of initial) this.data.set(keyOf(r), clone(r));
  }

  async open(): Promise<FileStatus> {
    this.isOpen = true;
    return '00';
  }

  async read(key: string): Promise<ReadResult<T>> {
    if (!this.isOpen) return { status: '48' };
    const rec = this.data.get(key);
    return rec ? { status: '00', record: clone(rec) } : { status: '23' };
  }

  async write(record: T): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    const key = this.keyOf(record);
    if (this.data.has(key)) return '22';
    this.data.set(key, clone(record));
    return '00';
  }

  async rewrite(record: T): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    const key = this.keyOf(record);
    if (!this.data.has(key)) return '23';
    this.data.set(key, clone(record));
    return '00';
  }

  async close(): Promise<FileStatus> {
    if (!this.isOpen) return '48';
    this.isOpen = false;
    return '00';
  }

  entries(): T[] {
    return [...this.data.keys()].sort().map((k) => clone(this.data.get(k) as T));
  }
}
