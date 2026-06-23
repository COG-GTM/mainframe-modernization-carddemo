/**
 * Card cross-reference entity — modern equivalent of copybook `CVACT03Y`
 * (VSAM file CCXREF / alternate index CXACAIX, record length 50).
 *
 * COBIL00C reads this through the CXACAIX alternate index keyed on account id
 * to discover the card number associated with the account being paid.
 */
export interface CardXref {
  /** XREF-CARD-NUM PIC X(16) */
  cardNumber: string;
  /** XREF-CUST-ID  PIC 9(09) */
  customerId: string;
  /** XREF-ACCT-ID  PIC 9(11) — alternate-index key used by COBIL00C */
  acctId: string;
}
