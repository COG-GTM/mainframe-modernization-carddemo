/**
 * EBCDIC (IBM code page 037) to ISO-8859-1 conversion for the sample data in
 * app/data/EBCDIC/, which the README instructs to upload in binary mode.
 */

/** CP037 byte -> Unicode code point (all of which are <= 0xFF). */
const CP037_TO_LATIN1: readonly number[] = [
  0, 1, 2, 3, 156, 9, 134, 127, 151, 141, 142, 11, 12, 13, 14, 15,
  16, 17, 18, 19, 157, 133, 8, 135, 24, 25, 146, 143, 28, 29, 30, 31,
  128, 129, 130, 131, 132, 10, 23, 27, 136, 137, 138, 139, 140, 5, 6, 7,
  144, 145, 22, 147, 148, 149, 150, 4, 152, 153, 154, 155, 20, 21, 158, 26,
  32, 160, 226, 228, 224, 225, 227, 229, 231, 241, 162, 46, 60, 40, 43, 124,
  38, 233, 234, 235, 232, 237, 238, 239, 236, 223, 33, 36, 42, 41, 59, 172,
  45, 47, 194, 196, 192, 193, 195, 197, 199, 209, 166, 44, 37, 95, 62, 63,
  248, 201, 202, 203, 200, 205, 206, 207, 204, 96, 58, 35, 64, 39, 61, 34,
  216, 97, 98, 99, 100, 101, 102, 103, 104, 105, 171, 187, 240, 253, 254, 177,
  176, 106, 107, 108, 109, 110, 111, 112, 113, 114, 170, 186, 230, 184, 198, 164,
  181, 126, 115, 116, 117, 118, 119, 120, 121, 122, 161, 191, 208, 221, 222, 174,
  94, 163, 165, 183, 169, 167, 182, 188, 189, 190, 91, 93, 175, 168, 180, 215,
  123, 65, 66, 67, 68, 69, 70, 71, 72, 73, 173, 244, 246, 242, 243, 245,
  125, 74, 75, 76, 77, 78, 79, 80, 81, 82, 185, 251, 252, 249, 250, 255,
  92, 247, 83, 84, 85, 86, 87, 88, 89, 90, 178, 212, 214, 210, 211, 213,
  48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 179, 219, 220, 217, 218, 159,
];

const LATIN1_TO_CP037 = new Uint8Array(256);
for (let byte = 0; byte < 256; byte += 1) {
  LATIN1_TO_CP037[CP037_TO_LATIN1[byte] as number] = byte;
}

/** Decodes EBCDIC (CP037) bytes into a latin1 string. */
export function decodeEbcdic(bytes: Buffer): Buffer {
  const out = Buffer.alloc(bytes.length);
  for (let i = 0; i < bytes.length; i += 1) {
    out[i] = CP037_TO_LATIN1[bytes[i] as number] as number;
  }
  return out;
}

/** Encodes a latin1 buffer back into EBCDIC (CP037) bytes. */
export function encodeEbcdic(bytes: Buffer): Buffer {
  const out = Buffer.alloc(bytes.length);
  for (let i = 0; i < bytes.length; i += 1) {
    out[i] = LATIN1_TO_CP037[bytes[i] as number] as number;
  }
  return out;
}
