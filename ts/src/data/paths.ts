import { join, resolve } from 'node:path';

/** Repository root (the directory holding `app/` and `ts/`). */
export const REPO_ROOT = resolve(__dirname, '..', '..', '..');

/** ASCII sample data shipped with the mainframe application. */
export const ASCII_DATA_DIR = join(REPO_ROOT, 'app', 'data', 'ASCII');

/** Working directory batch jobs write their output datasets to. */
export const WORK_DATA_DIR = join(REPO_ROOT, 'ts', 'data');

export const SAMPLE_FILES = {
  acctdata: join(ASCII_DATA_DIR, 'acctdata.txt'),
  carddata: join(ASCII_DATA_DIR, 'carddata.txt'),
  cardxref: join(ASCII_DATA_DIR, 'cardxref.txt'),
  custdata: join(ASCII_DATA_DIR, 'custdata.txt'),
  dailytran: join(ASCII_DATA_DIR, 'dailytran.txt'),
  discgrp: join(ASCII_DATA_DIR, 'discgrp.txt'),
  tcatbal: join(ASCII_DATA_DIR, 'tcatbal.txt'),
  trancatg: join(ASCII_DATA_DIR, 'trancatg.txt'),
  trantype: join(ASCII_DATA_DIR, 'trantype.txt'),
} as const;
