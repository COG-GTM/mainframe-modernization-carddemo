package com.carddemo.service.card;

/**
 * Verbatim screen messages ported from the three card programs. Kept as constants so the
 * service and the {@code ScreenHandler} beans emit exactly the wording the BMS screens show,
 * matching the {@code 88}-level message literals in {@code COCRDLIC}/{@code COCRDSLC}/
 * {@code COCRDUPC}.
 */
public final class CardMessages {

    private CardMessages() {
    }

    // ---- COCRDLIC (card list) --------------------------------------------------------

    /** COCRDLIC: account filter failed the 11-digit numeric edit ({@code 1000-EDIT}). */
    public static final String ACCOUNT_FILTER_INVALID =
        "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER";
    /** COCRDLIC: card filter failed the 16-digit numeric edit ({@code 1000-EDIT}). */
    public static final String CARD_FILTER_INVALID =
        "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER";
    /** COCRDLIC: browse returned nothing ({@code WS-NO-RECORDS-FOUND}). */
    public static final String NO_RECORDS_FOUND =
        "NO RECORDS FOUND FOR THIS SEARCH CONDITION.";
    /** COCRDLIC: selection field held a value other than {@code S}/{@code U}. */
    public static final String INVALID_ACTION_CODE = "INVALID ACTION CODE";
    /** COCRDLIC: more than one row selected on the list screen. */
    public static final String SELECT_ONLY_ONE =
        "PLEASE SELECT ONLY ONE RECORD TO VIEW OR UPDATE";

    // ---- COCRDSLC (card detail / select) ---------------------------------------------

    /** COCRDSLC: account key blank ({@code 2210-EDIT-ACCOUNT}). */
    public static final String ACCOUNT_NOT_PROVIDED = "Account number not provided";
    /** COCRDSLC: card key blank ({@code 2220-EDIT-CARD}). */
    public static final String CARD_NOT_PROVIDED = "Card number not provided";
    /** COCRDSLC/COCRDUPC: neither key supplied. */
    public static final String NO_INPUT_RECEIVED = "No input received";
    /** COCRDSLC/COCRDUPC: account not an 11-digit non-zero number. */
    public static final String ACCOUNT_NON_ZERO_11 =
        "Account number must be a non zero 11 digit number";
    /** COCRDSLC/COCRDUPC: card not a 16-digit number. */
    public static final String CARD_16_DIGIT =
        "Card number if supplied must be a 16 digit number";
    /** COCRDSLC/COCRDUPC: the account/card key matched no card record. */
    public static final String DID_NOT_FIND_CARDS =
        "Did not find cards for this search condition";
    /** COCRDUPC: the account was not found in the card cross-reference. */
    public static final String DID_NOT_FIND_ACCT =
        "Did not find this account in cards database";

    // ---- COCRDUPC (card update) ------------------------------------------------------

    /** COCRDUPC: embossed name blank ({@code 1230-EDIT-NAME}). */
    public static final String NAME_NOT_PROVIDED = "Card name not provided";
    /** COCRDUPC: embossed name has non-alphabetic characters ({@code 1230-EDIT-NAME}). */
    public static final String NAME_MUST_BE_ALPHA =
        "Card name can only contain alphabets and spaces";
    /** COCRDUPC: active-status flag not Y/N ({@code 1240-EDIT-CARDSTATUS}). */
    public static final String STATUS_MUST_BE_YES_NO = "Card Active Status must be Y or N";
    /** COCRDUPC: expiry month outside 1..12 ({@code 1250-EDIT-EXPIRY-MON}). */
    public static final String EXPIRY_MONTH_NOT_VALID =
        "Card expiry month must be between 1 and 12";
    /** COCRDUPC: expiry year outside 1950..2099 ({@code 1260-EDIT-EXPIRY-YEAR}). */
    public static final String EXPIRY_YEAR_NOT_VALID = "Invalid card expiry year";
    /** COCRDUPC: submitted values equal the fetched record ({@code NO-CHANGES-DETECTED}). */
    public static final String NO_CHANGES_DETECTED =
        "No change detected with respect to values fetched.";
    /** COCRDUPC: successful REWRITE ({@code CONFIRM-UPDATE-SUCCESS}). */
    public static final String UPDATE_SUCCESS = "Changes committed to database";
}
