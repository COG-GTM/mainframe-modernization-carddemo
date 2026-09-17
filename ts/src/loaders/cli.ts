/**
 * CLI for the data load jobs: `npm run seed -- [--ebcdic] [--data-dir <path>]`.
 *
 * Equivalent to running the load JCLs in app/jcl/ against a freshly defined set
 * of VSAM clusters.
 */

import { CardDemoStore, DEFAULT_DATA_DIR } from '../data/store.js';
import { seedAll, type SeedEncoding } from './seed.js';

function parseArgs(argv: readonly string[]): { encoding: SeedEncoding; dataDir: string } {
  let encoding: SeedEncoding = 'ascii';
  let dataDir = DEFAULT_DATA_DIR;

  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === '--ebcdic') encoding = 'ebcdic';
    else if (arg === '--ascii') encoding = 'ascii';
    else if (arg === '--data-dir') {
      const value = argv[i + 1];
      if (value === undefined) throw new Error('--data-dir requires a path');
      dataDir = value;
      i += 1;
    }
  }

  return { encoding, dataDir };
}

const { encoding, dataDir } = parseArgs(process.argv.slice(2));
const store = CardDemoStore.atPath(dataDir);
const results = seedAll(store, { encoding });
store.closeAll();

for (const result of results) {
  process.stdout.write(`${result.job.padEnd(18)} ${result.dataset.padEnd(9)} ${String(result.records).padStart(6)} records  <- ${result.source}\n`);
}
process.stdout.write(`\nDatasets written to ${dataDir}\n`);
