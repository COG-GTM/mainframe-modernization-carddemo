package com.carddemo.util;

import java.util.Objects;

/**
 * Immutable result of {@link DateValidator#validate(String, String)}, reproducing the two
 * outputs of the COBOL program {@code CSUTLDTC}:
 *
 * <ul>
 *   <li>the numeric {@code RETURN-CODE} (the LE severity) &mdash; see {@link #severity()}; and</li>
 *   <li>the 80-character {@code LS-RESULT} message layout ({@code WS-MESSAGE}) &mdash; see
 *       {@link #formattedMessage()}.</li>
 * </ul>
 *
 * <p>{@link #status()} exposes the mapped {@link DateValidationStatus} (feedback code) so callers
 * can branch on the specific failure without parsing the message text, and {@link #lilianDay()}
 * carries the {@code CEEDAYS} {@code OUTPUT-LILLIAN} day number when the date is valid.</p>
 */
public final class DateValidationResult {

    private final DateValidationStatus status;
    private final String inputDate;
    private final String format;
    private final Long lilianDay;
    private final String formattedMessage;

    DateValidationResult(DateValidationStatus status, String inputDate, String format,
                         Long lilianDay, String formattedMessage) {
        this.status = Objects.requireNonNull(status, "status");
        this.inputDate = inputDate;
        this.format = format;
        this.lilianDay = lilianDay;
        this.formattedMessage = formattedMessage;
    }

    /** The mapped feedback code / outcome. */
    public DateValidationStatus status() {
        return status;
    }

    /** {@code true} when the date is a valid calendar date in the supported range. */
    public boolean isValid() {
        return status.isValid();
    }

    /** LE severity moved into {@code RETURN-CODE} ({@code 0} success, {@code 3} error). */
    public int severity() {
        return status.severity();
    }

    /** LE message number ({@code WS-MSG-NO-N}); {@code 0} when the date is valid. */
    public int messageNumber() {
        return status.messageNumber();
    }

    /** The 15-character result literal ({@code WS-RESULT}), trimmed. */
    public String resultText() {
        return status.resultText();
    }

    /** The date string that was validated (as supplied). */
    public String inputDate() {
        return inputDate;
    }

    /** The format/picture string used for validation. */
    public String format() {
        return format;
    }

    /**
     * The {@code CEEDAYS} Lilian day number ({@code OUTPUT-LILLIAN}) &mdash; the count of days
     * with {@code 1582-10-15} being day {@code 1}. {@code null} unless the date is valid.
     */
    public Long lilianDay() {
        return lilianDay;
    }

    /**
     * The full 80-character {@code WS-MESSAGE} layout that {@code CSUTLDTC} returns in
     * {@code LS-RESULT} (severity, {@code Mesg Code:}, message number, result text, test date and
     * mask used).
     */
    public String formattedMessage() {
        return formattedMessage;
    }

    @Override
    public String toString() {
        return "DateValidationResult{status=" + status
                + ", severity=" + severity()
                + ", messageNumber=" + messageNumber()
                + ", resultText='" + resultText() + '\''
                + ", inputDate='" + inputDate + '\''
                + ", format='" + format + '\''
                + ", lilianDay=" + lilianDay
                + '}';
    }
}
