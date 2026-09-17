/**
 * WS-DATE-TIME (copybook CSDAT01Y) — the current date/time renderings the
 * online programs put on every screen header.
 */

function pad(value: number, length: number): string {
  return String(value).padStart(length, '0');
}

export interface CurrentDateTime {
  /** WS-CURDATE, PIC 9(08) as CCYYMMDD. */
  curdate: string;
  /** WS-CURTIME, PIC 9(08) as HHMMSSmm (hundredths). */
  curtime: string;
  /** WS-CURDATE-MM-DD-YY. */
  curdateMmDdYy: string;
  /** WS-CURTIME-HH-MM-SS. */
  curtimeHhMmSs: string;
  /** WS-TIMESTAMP, 'YYYY-MM-DD HH:MM:SS.ssssss'. */
  timestamp: string;
}

export function currentDateTime(now: Date = new Date()): CurrentDateTime {
  const yyyy = now.getFullYear();
  const mm = now.getMonth() + 1;
  const dd = now.getDate();
  const hh = now.getHours();
  const mi = now.getMinutes();
  const ss = now.getSeconds();
  const ms = now.getMilliseconds();
  return {
    curdate: `${pad(yyyy, 4)}${pad(mm, 2)}${pad(dd, 2)}`,
    curtime: `${pad(hh, 2)}${pad(mi, 2)}${pad(ss, 2)}${pad(Math.floor(ms / 10), 2)}`,
    curdateMmDdYy: `${pad(mm, 2)}/${pad(dd, 2)}/${pad(yyyy % 100, 2)}`,
    curtimeHhMmSs: `${pad(hh, 2)}:${pad(mi, 2)}:${pad(ss, 2)}`,
    timestamp: `${pad(yyyy, 4)}-${pad(mm, 2)}-${pad(dd, 2)} ${pad(hh, 2)}:${pad(mi, 2)}:${pad(
      ss,
      2,
    )}.${pad(ms * 1000, 6)}`,
  };
}

/** The 26 byte TRAN-ORIG-TS / TRAN-PROC-TS timestamp format. */
export function transactionTimestamp(now: Date = new Date()): string {
  return currentDateTime(now).timestamp.padEnd(26, ' ');
}
