import { Text } from './Screen.js';

/** CCDA-TITLE01 / CCDA-TITLE02 — `app/cpy/COTTL01Y.cpy`. */
export const TITLE01 = 'Mainframe Modernization';
export const TITLE02 = 'CardDemo';

function pad(value: number): string {
  return String(value).padStart(2, '0');
}

/** CURDATE — `mm/dd/yy`. */
export function screenDate(now: Date): string {
  return `${pad(now.getMonth() + 1)}/${pad(now.getDate())}/${pad(now.getFullYear() % 100)}`;
}

/** CURTIME — `hh:mm:ss`. */
export function screenTime(now: Date): string {
  return `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
}

/**
 * Rows 1-2 of every map: Tran / Prog on the left, the two title lines in the
 * middle, Date / Time on the right. COSGN00 puts the captions one column
 * wider than COMEN01 and COACTVW, hence `spaced`.
 */
export function Header({
  tranId,
  program,
  now,
  spaced = false,
}: {
  tranId: string;
  program: string;
  now: Date;
  spaced?: boolean;
}): JSX.Element {
  const captionLen = spaced ? 6 : 5;
  const valueCol = spaced ? 8 : 7;
  const rightCaptionCol = spaced ? 64 : 65;

  return (
    <>
      <Text row={1} col={1} len={captionLen}>
        {spaced ? 'Tran :' : 'Tran:'}
      </Text>
      <Text row={1} col={valueCol} len={4}>
        {tranId}
      </Text>
      <Text row={1} col={21} len={40} color="yellow">
        {TITLE01}
      </Text>
      <Text row={1} col={rightCaptionCol} len={captionLen}>
        {spaced ? 'Date :' : 'Date:'}
      </Text>
      <Text row={1} col={71} len={8}>
        {screenDate(now)}
      </Text>

      <Text row={2} col={1} len={captionLen}>
        {spaced ? 'Prog :' : 'Prog:'}
      </Text>
      <Text row={2} col={valueCol} len={8}>
        {program}
      </Text>
      <Text row={2} col={21} len={40} color="yellow">
        {TITLE02}
      </Text>
      <Text row={2} col={rightCaptionCol} len={captionLen}>
        {spaced ? 'Time :' : 'Time:'}
      </Text>
      <Text row={2} col={71} len={9}>
        {screenTime(now)}
      </Text>
    </>
  );
}

/** ERRMSG at POS=(23,1) LENGTH=78 plus the function-key line at row 24. */
export function Footer({
  errorMessage,
  keys,
}: {
  errorMessage: string;
  keys: string;
}): JSX.Element {
  return (
    <>
      <Text row={23} col={1} len={78} color="red" bright>
        {errorMessage}
      </Text>
      <Text row={24} col={1} len={60} color="yellow">
        {keys}
      </Text>
    </>
  );
}
