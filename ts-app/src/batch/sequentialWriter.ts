import { closeSync, openSync, writeSync } from 'node:fs';
import type { SequentialWriter } from '../data/repositories.js';

/** In-memory sink for fixed-width output lines, so tests never touch disk. */
export class InMemoryLineWriter implements SequentialWriter<string> {
  private readonly lines: string[] = [];

  write(record: string): void {
    this.lines.push(record);
  }

  records(): string[] {
    return [...this.lines];
  }
}

/**
 * QSAM-style sequential output file: one fixed-width record per line,
 * as the `DALYREJS` DD of `app/jcl/POSTTRAN.jcl` produces.
 */
export class FileLineWriter implements SequentialWriter<string> {
  private readonly fd: number;
  private readonly lines: string[] = [];

  constructor(path: string) {
    this.fd = openSync(path, 'w');
  }

  write(record: string): void {
    this.lines.push(record);
    writeSync(this.fd, `${record}\n`);
  }

  records(): string[] {
    return [...this.lines];
  }

  close(): void {
    closeSync(this.fd);
  }
}
