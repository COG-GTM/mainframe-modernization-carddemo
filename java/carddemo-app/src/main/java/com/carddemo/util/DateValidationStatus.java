package com.carddemo.util;

/**
 * The outcome of a date validation, mapped one-to-one from the {@code CEEDAYS} feedback codes
 * that the COBOL program {@code CSUTLDTC} inspects.
 *
 * <p>Each constant carries the same three pieces of information {@code CSUTLDTC} surfaces:</p>
 * <ul>
 *   <li>{@link #severity()} &mdash; the LE condition-token severity moved into {@code RETURN-CODE}
 *       ({@code WS-SEVERITY-N}); {@code 0} means success, {@code 3} an error.</li>
 *   <li>{@link #messageNumber()} &mdash; the LE message number moved into {@code WS-MSG-NO-N}
 *       (a {@code CEExxxx} number; e.g. {@code 2508} is {@code CEE2508}).</li>
 *   <li>{@link #resultText()} &mdash; the 15-character result literal placed in {@code WS-RESULT}
 *       by the {@code EVALUATE} in {@code A000-MAIN}.</li>
 * </ul>
 *
 * <p>The {@link #feedbackToken()} hex string is the exact 8-byte {@code FEEDBACK-TOKEN-VALUE}
 * level-88 constant declared in {@code CSUTLDTC} (severity, message number, case/severity
 * control byte {@code 0x59} and the {@code CEE} facility id {@code 0xC3C5C5} in EBCDIC), kept
 * here for traceability back to the COBOL source.</p>
 */
public enum DateValidationStatus {

    /** {@code FC-INVALID-DATE} X'0000000000000000' &rarr; {@code "Date is valid"}. */
    VALID(0, 0, "Date is valid", "0000000000000000"),

    /** {@code FC-INSUFFICIENT-DATA} X'000309CB59C3C5C5' &rarr; {@code "Insufficient"}. */
    INSUFFICIENT_DATA(3, 2507, "Insufficient", "000309CB59C3C5C5"),

    /** {@code FC-BAD-DATE-VALUE} X'000309CC59C3C5C5' &rarr; {@code "Datevalue error"}. */
    BAD_DATE_VALUE(3, 2508, "Datevalue error", "000309CC59C3C5C5"),

    /** {@code FC-INVALID-ERA} X'000309CD59C3C5C5' &rarr; {@code "Invalid Era"}. */
    INVALID_ERA(3, 2509, "Invalid Era", "000309CD59C3C5C5"),

    /** {@code FC-UNSUPP-RANGE} X'000309D159C3C5C5' &rarr; {@code "Unsupp. Range"}. */
    UNSUPPORTED_RANGE(3, 2513, "Unsupp. Range", "000309D159C3C5C5"),

    /** {@code FC-INVALID-MONTH} X'000309D559C3C5C5' &rarr; {@code "Invalid month"}. */
    INVALID_MONTH(3, 2517, "Invalid month", "000309D559C3C5C5"),

    /** {@code FC-BAD-PIC-STRING} X'000309D659C3C5C5' &rarr; {@code "Bad Pic String"}. */
    BAD_PICTURE_STRING(3, 2518, "Bad Pic String", "000309D659C3C5C5"),

    /** {@code FC-NON-NUMERIC-DATA} X'000309D859C3C5C5' &rarr; {@code "Nonnumeric data"}. */
    NONNUMERIC_DATA(3, 2520, "Nonnumeric data", "000309D859C3C5C5"),

    /** {@code FC-YEAR-IN-ERA-ZERO} X'000309D959C3C5C5' &rarr; {@code "YearInEra is 0"}. */
    YEAR_IN_ERA_ZERO(3, 2521, "YearInEra is 0", "000309D959C3C5C5"),

    /**
     * The {@code WHEN OTHER} catch-all of {@code CSUTLDTC}: any feedback code that is not one of
     * the recognised constants above yields {@code "Date is invalid"}. Modelled with severity
     * {@code 3} (an error) so callers treat it like the other failures.
     */
    INVALID_DATE(3, 0, "Date is invalid", null);

    private final int severity;
    private final int messageNumber;
    private final String resultText;
    private final String feedbackToken;

    DateValidationStatus(int severity, int messageNumber, String resultText, String feedbackToken) {
        this.severity = severity;
        this.messageNumber = messageNumber;
        this.resultText = resultText;
        this.feedbackToken = feedbackToken;
    }

    /** LE severity ({@code 0} success, {@code 3} error); becomes {@code RETURN-CODE} in COBOL. */
    public int severity() {
        return severity;
    }

    /** LE message number (a {@code CEExxxx} number); becomes {@code WS-MSG-NO-N} in COBOL. */
    public int messageNumber() {
        return messageNumber;
    }

    /** The 15-character result literal from the {@code CSUTLDTC} {@code EVALUATE} (trimmed). */
    public String resultText() {
        return resultText;
    }

    /**
     * The 8-byte {@code FEEDBACK-TOKEN-VALUE} hex constant from {@code CSUTLDTC}, or {@code null}
     * for {@link #INVALID_DATE} (the {@code WHEN OTHER} branch, which has no fixed token).
     */
    public String feedbackToken() {
        return feedbackToken;
    }

    /** {@code true} only for {@link #VALID} &mdash; i.e. {@code severity == 0}. */
    public boolean isValid() {
        return severity == 0;
    }
}
