/**
 * Mapping of the sample data files in app/data/ onto the migrated datasets,
 * replicating the IDCAMS REPRO load steps in app/jcl/ (ACCTFILE, CARDFILE,
 * CUSTFILE, XREFFILE, TRANFILE, DISCGRP, TCATBALF, TRANCATG, TRANTYPE) and the
 * in-stream user security data in app/jcl/DUSRSECJ.jcl.
 */

import type { KsdsName, SequentialName } from '../data/catalog.js';

export interface SeedSource {
  /** Load job in app/jcl that this reproduces. */
  readonly job: string;
  readonly dataset: KsdsName | SequentialName;
  readonly kind: 'ksds' | 'sequential';
  readonly recordLength: number;
  /** File name under app/data/ASCII. */
  readonly asciiFile?: string;
  /** File name under app/data/EBCDIC. */
  readonly ebcdicFile?: string;
}

export const SEED_SOURCES: readonly SeedSource[] = [
  {
    job: 'ACCTFILE',
    dataset: 'ACCTDAT',
    kind: 'ksds',
    recordLength: 300,
    asciiFile: 'acctdata.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.ACCTDATA.PS',
  },
  {
    job: 'CARDFILE',
    dataset: 'CARDDAT',
    kind: 'ksds',
    recordLength: 150,
    asciiFile: 'carddata.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.CARDDATA.PS',
  },
  {
    job: 'CUSTFILE',
    dataset: 'CUSTDAT',
    kind: 'ksds',
    recordLength: 500,
    asciiFile: 'custdata.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.CUSTDATA.PS',
  },
  {
    job: 'XREFFILE',
    dataset: 'CCXREF',
    kind: 'ksds',
    recordLength: 50,
    asciiFile: 'cardxref.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.CARDXREF.PS',
  },
  {
    job: 'TRANFILE',
    dataset: 'TRANSACT',
    kind: 'ksds',
    recordLength: 350,
    ebcdicFile: 'AWS.M2.CARDDEMO.DALYTRAN.PS.INIT',
  },
  {
    job: 'DISCGRP',
    dataset: 'DISCGRP',
    kind: 'ksds',
    recordLength: 50,
    asciiFile: 'discgrp.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.DISCGRP.PS',
  },
  {
    job: 'TCATBALF',
    dataset: 'TCATBALF',
    kind: 'ksds',
    recordLength: 50,
    asciiFile: 'tcatbal.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.TCATBALF.PS',
  },
  {
    job: 'TRANCATG',
    dataset: 'TRANCATG',
    kind: 'ksds',
    recordLength: 60,
    asciiFile: 'trancatg.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.TRANCATG.PS',
  },
  {
    job: 'TRANTYPE',
    dataset: 'TRANTYPE',
    kind: 'ksds',
    recordLength: 60,
    asciiFile: 'trantype.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.TRANTYPE.PS',
  },
  {
    job: 'DUSRSECJ',
    dataset: 'USRSEC',
    kind: 'ksds',
    recordLength: 80,
    ebcdicFile: 'AWS.M2.CARDDEMO.USRSEC.PS',
  },
  {
    job: 'POSTTRAN (input)',
    dataset: 'DALYTRAN',
    kind: 'sequential',
    recordLength: 350,
    asciiFile: 'dailytran.txt',
    ebcdicFile: 'AWS.M2.CARDDEMO.DALYTRAN.PS',
  },
];

/**
 * In-stream user security records from app/jcl/DUSRSECJ.jcl (SYSUT1), used when
 * no USRSEC file is available under app/data.
 */
export const INLINE_USRSEC_RECORDS: readonly string[] = [
  'ADMIN001MARGARET            GOLD                PASSWORDA',
  'ADMIN002RUSSELL             RUSSELL             PASSWORDA',
  'ADMIN003RAYMOND             WHITMORE            PASSWORDA',
  'ADMIN004EMMANUEL            CASGRAIN            PASSWORDA',
  'ADMIN005GRANVILLE           LACHAPELLE          PASSWORDA',
  'USER0001LAWRENCE            THOMAS              PASSWORDU',
  'USER0002AJITH               KUMAR               PASSWORDU',
  'USER0003LAURITZ             ALME                PASSWORDU',
  'USER0004AVERARDO            MAZZI               PASSWORDU',
  'USER0005LEE                 TING                PASSWORDU',
].map((record) => record.padEnd(80, ' '));

/**
 * Initial TRANSACT record from AWS.M2.CARDDEMO.DALYTRAN.PS.INIT: low-values
 * with the trailing 8 bytes set to '00000100' (see the README dataset table).
 */
export const INLINE_TRANSACT_INIT_RECORD = '\u0000'.repeat(342) + '00000100';
