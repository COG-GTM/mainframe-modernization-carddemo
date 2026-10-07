/**
 * Pluggable data-access layer replacing the VSAM/QSAM files of the POSTTRAN
 * job. Implementations report COBOL FILE STATUS codes so program logic can be
 * ported paragraph-for-paragraph; a database-backed implementation only has to
 * map its outcomes onto the same codes.
 */
export type FileStatus =
  /** successful completion */
  | '00'
  /** end of file (sequential READ) */
  | '10'
  /** duplicate key (WRITE to KSDS) */
  | '22'
  /** record not found (READ / REWRITE by key) */
  | '23'
  /** permanent I/O error */
  | '30'
  /** file not found on OPEN */
  | '35'
  /** logic error (e.g. operation on a file that is not open) */
  | '48';

export interface ReadResult<T> {
  status: FileStatus;
  record?: T;
}

/** QSAM / sequential input dataset (e.g. DALYTRAN). */
export interface SequentialInputFile<T> {
  open(): Promise<FileStatus>;
  read(): Promise<ReadResult<T>>;
  close(): Promise<FileStatus>;
}

/** QSAM / sequential output dataset (e.g. DALYREJS). */
export interface SequentialOutputFile<T> {
  open(): Promise<FileStatus>;
  write(record: T): Promise<FileStatus>;
  close(): Promise<FileStatus>;
}

/** VSAM KSDS opened for random access (ACCESS MODE IS RANDOM). */
export interface KeyedFile<T> {
  open(): Promise<FileStatus>;
  /** READ ... KEY IS – '00' found, '23' not found. */
  read(key: string): Promise<ReadResult<T>>;
  /** WRITE – '00' ok, '22' duplicate key. */
  write(record: T): Promise<FileStatus>;
  /** REWRITE – '00' ok, '23' record not found. */
  rewrite(record: T): Promise<FileStatus>;
  close(): Promise<FileStatus>;
}
