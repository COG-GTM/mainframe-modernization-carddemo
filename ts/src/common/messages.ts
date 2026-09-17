/** CCDA-COMMON-MESSAGES (copybook CSMSG01Y) and ABEND-DATA (copybook CSMSG02Y). */

export const CCDA_MSG_THANK_YOU = 'Thank you for using CardDemo application...';
export const CCDA_MSG_INVALID_KEY = 'Invalid key pressed. Please see below...';

/** CCDA-SCREEN-TITLE (copybook COTTL01Y), each title PIC X(40). */
export const CCDA_TITLE01 = '        Mainframe Modernization        ';
export const CCDA_TITLE02 = '              CardDemo                  ';
export const CCDA_THANK_YOU = 'Thank you for using CCDA application... ';

/** ABEND-DATA (copybook CSMSG02Y): X(4) + X(8) + X(50) + X(72). */
export interface AbendData {
  abendCode: string;
  abendCulprit: string;
  abendReason: string;
  abendMsg: string;
}

export function emptyAbendData(): AbendData {
  return { abendCode: '', abendCulprit: '', abendReason: '', abendMsg: '' };
}
