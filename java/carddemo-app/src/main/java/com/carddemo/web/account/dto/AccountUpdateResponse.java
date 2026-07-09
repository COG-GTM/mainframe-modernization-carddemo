package com.carddemo.web.account.dto;

/**
 * Response DTO for the account update screen. Mirrors what {@code COACTUPC} leaves on the
 * {@code COACTUP} map after a turn: an information/confirmation message
 * ({@code WS-INFO-MSG}/{@code WS-RETURN-MSG}) plus the account/customer detail as it now
 * stands.
 *
 * <ul>
 *   <li>On a successful commit ({@code CONFIRM-UPDATE-SUCCESS}) {@code message} is
 *       {@code "Changes committed to database"} and {@code account} reflects the saved
 *       values.</li>
 *   <li>When the submitted values match what is on file ({@code NO-CHANGES-DETECTED})
 *       {@code message} is {@code "No change detected with respect to values fetched."} and
 *       {@code account} is the unchanged record.</li>
 * </ul>
 */
public record AccountUpdateResponse(String message, AccountViewResponse account) {
}
