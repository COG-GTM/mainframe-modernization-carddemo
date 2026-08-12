package com.carddemo.online.common;

/**
 * COBOL copybook: CSUTLDWY — the outcome flags of the CSUTLDPY edit paragraphs.
 *
 * <p>{@code WS-EDIT-DATE-FLGS} holds one character per component; LOW-VALUES means valid,
 * {@code '0'} not OK and {@code 'B'} blank. {@link #dateFlagsValid()} is the
 * {@code WS-EDIT-DATE-IS-VALID} condition (all three flags LOW-VALUES) and {@link #inputError()}
 * is the {@code INPUT-ERROR} switch the callers test.
 *
 * @param inputError INPUT-ERROR was set by one of the edits
 * @param dateFlagsValid WS-EDIT-DATE-IS-VALID at the end of EDIT-DATE-CCYYMMDD
 * @param returnMessage WS-RETURN-MSG, the first message produced (empty when none)
 * @param yearFlag WS-EDIT-YEAR-FLG
 * @param monthFlag WS-EDIT-MONTH
 * @param dayFlag WS-EDIT-DAY
 */
public record DateValidationResult(
        boolean inputError,
        boolean dateFlagsValid,
        String returnMessage,
        char yearFlag,
        char monthFlag,
        char dayFlag) {

    /** LOW-VALUES, i.e. the component passed its edits. */
    public static final char FLAG_VALID = '\u0000';

    /** FLG-*-NOT-OK. */
    public static final char FLAG_NOT_OK = '0';

    /** FLG-*-BLANK. */
    public static final char FLAG_BLANK = 'B';

    /** True when the callers' {@code IF NOT INPUT-ERROR} branch would be taken. */
    public boolean isValid() {
        return !inputError;
    }
}
