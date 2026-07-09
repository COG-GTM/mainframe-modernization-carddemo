package com.carddemo.web.card.dto;

/**
 * Editable card fields submitted from the card-update screen ({@code COCRDUP} /
 * {@code COCRDUPC}).
 *
 * <p>{@code COCRDUPC} only lets the user change the embossed name, the active-status flag
 * and the expiry month/year (map fields {@code CRDNAME}, {@code CRDSTCD}, {@code EXPMON},
 * {@code EXPYEAR}); the account number and card number are the read key and the expiry day,
 * CVV and account id are carried unchanged from the fetched record. Values are kept as
 * {@link String} because the COBOL edits validate them as fixed-width text (see the
 * {@code 1230}/{@code 1240}/{@code 1250}/{@code 1260} EDIT paragraphs).</p>
 *
 * @param embossedName CCUP-NEW-CRDNAME PIC X(50) — alphabetic and spaces only.
 * @param activeStatus CCUP-NEW-CRDSTCD PIC X(01) — {@code Y} or {@code N}.
 * @param expiryMonth  CCUP-NEW-EXPMON PIC X(02) — {@code 01}..{@code 12}.
 * @param expiryYear   CCUP-NEW-EXPYEAR PIC X(04) — {@code 1950}..{@code 2099}.
 */
public record CardUpdateRequest(
        String embossedName,
        String activeStatus,
        String expiryMonth,
        String expiryYear) {
}
