/**
 * Request / response DTOs for the Bill Payment screen.
 *
 * Derived from the BMS symbolic map `COBIL00.CPY` (map COBIL0A) and the
 * COMMAREA `COCOM01Y` / `CDEMO-CB00-INFO` that COBIL00C reads and writes.
 *
 * The original 3270 screen (transaction CB00) exposes exactly three editable
 * fields — account id, current balance (display only) and a Y/N confirm flag.
 */

/** Y/N confirmation flag from the BMS field CONFIRMI (`PIC X(1)`). */
export type ConfirmFlag = 'Y' | 'N' | '';

export interface BillPaymentRequest {
  /** ACTIDINI `PIC X(11)` — account whose balance is being paid. */
  acctId: string;
  /**
   * CONFIRMI `PIC X(1)` — 'Y' executes the payment, 'N' clears the screen,
   * blank only looks up and displays the balance. Case-insensitive in COBIL00C.
   */
  confirm?: string;
}

export interface BillPaymentResponse {
  /** Whether a payment transaction was actually written. */
  paid: boolean;
  /** ACTIDINI echoed back. */
  acctId: string;
  /** CURBALO — account balance after processing, formatted to 2 decimals. */
  currentBalance: string;
  /** ERRMSGO — status / error message shown on the screen. */
  message: string;
  /** TRAN-ID of the written bill-payment transaction (only when paid). */
  transactionId?: string;
}
