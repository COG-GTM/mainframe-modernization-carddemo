/**
 * File access failures, named after the CICS RESP2 conditions the COBOL
 * programs test for after EXEC CICS READ/WRITE/REWRITE/DELETE.
 */

export class FileError extends Error {
  constructor(
    readonly file: string,
    message: string,
  ) {
    super(message);
    this.name = new.target.name;
  }
}

/** DFHRESP(NOTFND) */
export class RecordNotFoundError extends FileError {
  constructor(file: string, key: string) {
    super(file, `record not found in ${file} for key '${key}'`);
  }
}

/** DFHRESP(DUPREC) */
export class DuplicateRecordError extends FileError {
  constructor(file: string, key: string) {
    super(file, `duplicate record in ${file} for key '${key}'`);
  }
}

/** DFHRESP(ENDFILE) */
export class EndOfFileError extends FileError {
  constructor(file: string) {
    super(file, `end of file reached on ${file}`);
  }
}
