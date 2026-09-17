import { formatPicSignedZ, formatPicPlusZ } from '../common/picFormat';

/**
 * Daily transaction report layouts (copybook CVTRA07Y) used by CBTRN03C.
 * Each line is a fixed 133 byte print record.
 */

export const REPT_SHORT_NAME = 'DALYREPT';
export const REPT_LONG_NAME = 'Daily Transaction Report';
export const REPT_DATE_HEADER = 'Date Range: ';

/** REPORT-NAME-HEADER: X(38) + X(41) + X(12) + X(10) + ' to ' + X(10). */
export function reportNameHeader(startDate: string, endDate: string): string {
  return (
    REPT_SHORT_NAME.padEnd(38, ' ') +
    REPT_LONG_NAME.padEnd(41, ' ') +
    REPT_DATE_HEADER.padEnd(12, ' ') +
    startDate.padEnd(10, ' ') +
    ' to ' +
    endDate.padEnd(10, ' ')
  );
}

/** TRANSACTION-HEADER-1. */
export const TRANSACTION_HEADER_1 =
  'Transaction ID'.padEnd(17, ' ') +
  'Account ID'.padEnd(12, ' ') +
  'Transaction Type'.padEnd(19, ' ') +
  'Tran Category'.padEnd(35, ' ') +
  'Tran Source'.padEnd(14, ' ') +
  ' ' +
  '        Amount'.padEnd(16, ' ');

/** TRANSACTION-HEADER-2: PIC X(133) VALUE ALL '-'. */
export const TRANSACTION_HEADER_2 = '-'.repeat(133);

export interface TransactionDetailReportLine {
  tranReportTransId: string;
  tranReportAccountId: string;
  tranReportTypeCd: string;
  tranReportTypeDesc: string;
  tranReportCatCd: number;
  tranReportCatDesc: string;
  tranReportSource: string;
  tranReportAmt: number;
}

/**
 * TRANSACTION-DETAIL-REPORT:
 * X(16) ' ' X(11) ' ' X(02) '-' X(15) ' ' 9(04) '-' X(29) ' ' X(10) '    '
 * -ZZZ,ZZZ,ZZZ.ZZ '  '
 */
export function transactionDetailReportLine(line: TransactionDetailReportLine): string {
  return (
    line.tranReportTransId.padEnd(16, ' ') +
    ' ' +
    line.tranReportAccountId.padEnd(11, ' ') +
    ' ' +
    line.tranReportTypeCd.padEnd(2, ' ') +
    '-' +
    line.tranReportTypeDesc.padEnd(15, ' ') +
    ' ' +
    String(line.tranReportCatCd).padStart(4, '0') +
    '-' +
    line.tranReportCatDesc.padEnd(29, ' ') +
    ' ' +
    line.tranReportSource.padEnd(10, ' ') +
    '    ' +
    formatPicSignedZ(line.tranReportAmt) +
    '  '
  );
}

/** REPORT-PAGE-TOTALS: 'Page Total' X(11) + 86 dots + +ZZZ,ZZZ,ZZZ.ZZ. */
export function reportPageTotals(total: number): string {
  return 'Page Total'.padEnd(11, ' ') + '.'.repeat(86) + formatPicPlusZ(total);
}

/** REPORT-ACCOUNT-TOTALS: 'Account Total' X(13) + 84 dots + +ZZZ,ZZZ,ZZZ.ZZ. */
export function reportAccountTotals(total: number): string {
  return 'Account Total'.padEnd(13, ' ') + '.'.repeat(84) + formatPicPlusZ(total);
}

/** REPORT-GRAND-TOTALS: 'Grand Total' X(11) + 86 dots + +ZZZ,ZZZ,ZZZ.ZZ. */
export function reportGrandTotals(total: number): string {
  return 'Grand Total'.padEnd(11, ' ') + '.'.repeat(86) + formatPicPlusZ(total);
}
