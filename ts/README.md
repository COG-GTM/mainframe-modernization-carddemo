# CardDemo TypeScript migration

TypeScript port of the COBOL/CICS/VSAM application under `app/`. This directory
holds the foundation layer (phase 1): COBOL runtime primitives, record models
generated from the copybooks, a data-access layer replacing VSAM, and the data
loaders replacing the load JCLs.

## Layout

| Path | Contents | Migrated from |
| :--- | :------- | :------------ |
| `src/cobol/` | PIC parsing, fixed-length record codec, zoned/packed decimal, EBCDIC codec, fixed-point arithmetic | COBOL language runtime |
| `src/models/` | One interface + `RecordSpec` per record layout | `app/cpy/*.cpy` |
| `src/data/` | KSDS/sequential dataset access, dataset catalog, file-status errors | VSAM clusters defined in `app/jcl/*.jcl` |
| `src/loaders/` | Sample-data seeding | `app/jcl/ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, DISCGRP, TCATBALF, TRANCATG, TRANTYPE, DUSRSECJ` |
| `src/batch/` | Batch programs (phase 2) | `app/cbl/CB*.cbl` |
| `src/online/` | Online transactions and their screens (phase 2) | `app/cbl/CO*.cbl` + `app/bms/*.bms` |
| `src/jobs/` | Job runner sequencing the batch programs (phase 2) | `app/jcl/`, `app/proc/` |

## Commands

```bash
npm install
npm run generate:models   # regenerate src/models from app/cpy
npm run seed              # load app/data/ASCII into ./data (add --ebcdic for the EBCDIC copies)
npm test
npm run lint
npm run build
```

## Conventions for migrated programs

- Every generated file names the COBOL source it came from in a header comment.
- Business logic and arithmetic must match the COBOL exactly. Use
  `src/cobol/decimal.ts` for money arithmetic (`ROUNDED` is half-up away from
  zero) rather than raw `+`/`*` on floats.
- Data access goes through `CardDemoStore` only:

  ```ts
  import { CardDemoStore } from '../data/store.js';

  const store = CardDemoStore.atPath('./data');
  const accounts = store.open('ACCTDAT');        // KSDS: read/startBrowse/write/rewrite/delete
  const cards = store.open('CARDDAT').readByAlternateIndex('CARDAIX', acctId);
  const daily = store.openSequential('DALYTRAN'); // QSAM: readNext/write
  ```

- File-status branching uses `DatasetError.status` with the codes in
  `src/data/status.ts` (`00`, `10`, `22`, `23`), matching the COBOL `FILE STATUS`
  checks.
- Numeric fields decode to `number` with the copybook's implied scale; text
  fields decode to space-trimmed `string`. `encodeRecord` restores the exact
  fixed-length representation, including zoned-decimal overpunch signs.
- Online programs replace the CICS pseudo-conversational flow: BMS maps become
  REST endpoints returning JSON, and the `COCOM01Y` COMMAREA becomes the
  server-side session object in `src/models/commarea.ts`.
