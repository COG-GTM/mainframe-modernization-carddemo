package com.carddemo.online.common;

import java.time.DateTimeException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * COBOL program: CSUTLDTC — date validation utility (call to the Language Environment service
 * CEEDAYS), together with the edit paragraphs of copybook CSUTLDPY and the working storage of
 * copybook CSUTLDWY.
 *
 * <p>{@link #validateCcyymmdd(String, String)} reproduces
 * {@code PERFORM EDIT-DATE-CCYYMMDD THRU EDIT-DATE-CCYYMMDD-EXIT}: every paragraph is executed in
 * order (a failing edit only branches to its own exit label and falls through to the next one) and
 * {@code WS-RETURN-MSG} keeps the first message produced, because CSUTLDPY only writes it while
 * {@code WS-RETURN-MSG-OFF}.
 *
 * <p>{@link #validateWithLanguageEnvironment(String, String)} stands in for CEEDAYS itself. The
 * feedback tokens of CSUTLDTC encode the condition as two halfwords, so the severity and message
 * number are taken from them directly (for example {@code X'000309CB59C3C5C5'} =
 * severity 3, message 2507 = insufficient data). Only the {@code YYYYMMDD} mask that CSUTLDPY
 * passes is supported; any other mask answers the CEEDAYS "bad picture string" condition.
 */
@Service
public class DateValidationService {

    /** WS-DATE-FORMAT of CSUTLDWY, the only mask CSUTLDPY passes to CSUTLDTC. */
    public static final String FORMAT_CCYYMMDD = "YYYYMMDD";

    /** CEEDAYS feedback codes listed in the FEEDBACK-TOKEN-VALUE 88-levels of CSUTLDTC. */
    private enum FeedbackCode {
        /** FC-INVALID-DATE X'0000000000000000' — the "no condition" token: the date is valid. */
        VALID(0, 0, "Date is valid"),
        /** FC-INSUFFICIENT-DATA X'000309CB59C3C5C5'. */
        INSUFFICIENT_DATA(3, 0x09CB, "Insufficient"),
        /** FC-BAD-DATE-VALUE X'000309CC59C3C5C5'. */
        BAD_DATE_VALUE(3, 0x09CC, "Datevalue error"),
        /** FC-INVALID-ERA X'000309CD59C3C5C5'. */
        INVALID_ERA(3, 0x09CD, "Invalid Era    "),
        /** FC-UNSUPP-RANGE X'000309D159C3C5C5'. */
        UNSUPPORTED_RANGE(3, 0x09D1, "Unsupp. Range  "),
        /** FC-INVALID-MONTH X'000309D559C3C5C5'. */
        INVALID_MONTH(3, 0x09D5, "Invalid month  "),
        /** FC-BAD-PIC-STRING X'000309D659C3C5C5'. */
        BAD_PIC_STRING(3, 0x09D6, "Bad Pic String "),
        /** FC-NON-NUMERIC-DATA X'000309D859C3C5C5'. */
        NON_NUMERIC_DATA(3, 0x09D8, "Nonnumeric data"),
        /** FC-YEAR-IN-ERA-ZERO X'000309D959C3C5C5'. */
        YEAR_IN_ERA_ZERO(3, 0x09D9, "YearInEra is 0 ");

        private final int severity;
        private final int messageNumber;
        private final String resultText;

        FeedbackCode(int severity, int messageNumber, String resultText) {
            this.severity = severity;
            this.messageNumber = messageNumber;
            this.resultText = resultText;
        }
    }

    /**
     * COBOL paragraphs: EDIT-DATE-CCYYMMDD through EDIT-DATE-CCYYMMDD-EXIT of CSUTLDPY.
     *
     * @param ccyymmdd WS-EDIT-DATE-CCYYMMDD PIC X(8)
     * @param variableName WS-EDIT-VARIABLE-NAME, prefixed to every message
     */
    public DateValidationResult validateCcyymmdd(String ccyymmdd, String variableName) {
        EditState state = new EditState(pad(ccyymmdd, 8), trim(variableName));

        editYearCcyy(state);
        editMonth(state);
        editDay(state);
        if (editDayMonthYear(state)) {
            return state.result(false);
        }
        editDateLanguageEnvironment(state);
        // EDIT-DATE-LE-EXIT unconditionally performs SET WS-EDIT-DATE-IS-VALID, so the flag bytes
        // end up LOW-VALUES whenever the paragraph is reached; the callers rely on INPUT-ERROR.
        return state.result(true);
    }

    /**
     * COBOL paragraph: EDIT-DATE-OF-BIRTH of CSUTLDPY. The date must lie strictly in the past
     * ({@code WS-CURRENT-DATE-BINARY > WS-EDIT-DATE-BINARY}), so today is rejected too.
     */
    public DateValidationResult validateDateOfBirth(String ccyymmdd, String variableName) {
        return validateDateOfBirth(ccyymmdd, variableName, LocalDate.now());
    }

    DateValidationResult validateDateOfBirth(String ccyymmdd, String variableName, LocalDate today) {
        EditState state = new EditState(pad(ccyymmdd, 8), trim(variableName));
        LocalDate date = parseDate(state.ccyymmdd);
        if (date == null || !today.isAfter(date)) {
            state.inputError = true;
            state.dayFlag = DateValidationResult.FLAG_NOT_OK;
            state.monthFlag = DateValidationResult.FLAG_NOT_OK;
            state.yearFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName + ":cannot be in the future ");
        }
        return state.result(false);
    }

    /** COBOL program: CSUTLDTC (PROCEDURE DIVISION USING LS-DATE, LS-DATE-FORMAT, LS-RESULT). */
    public LanguageEnvironmentDateResult validateWithLanguageEnvironment(String date, String format) {
        FeedbackCode feedback = callCeedays(date, format);
        String message = "%04d".formatted(feedback.severity)
                + padRight("Mesg Code:", 11)
                + "%04d".formatted(feedback.messageNumber)
                + " "
                + padRight(feedback.resultText, 15)
                + " "
                + padRight("TstDate:", 9)
                + pad(date, 10)
                + " "
                + padRight("Mask used:", 10)
                + pad(format, 10)
                + " "
                + "   ";
        return new LanguageEnvironmentDateResult(
                feedback.severity, feedback.messageNumber, padRight(feedback.resultText, 15), message);
    }

    private FeedbackCode callCeedays(String date, String format) {
        if (!FORMAT_CCYYMMDD.equals(trim(format))) {
            return FeedbackCode.BAD_PIC_STRING;
        }
        String value = trim(date);
        if (value.length() < 8) {
            return FeedbackCode.INSUFFICIENT_DATA;
        }
        value = value.substring(0, 8);
        if (!isNumeric(value)) {
            return FeedbackCode.NON_NUMERIC_DATA;
        }
        int year = Integer.parseInt(value.substring(0, 4));
        int month = Integer.parseInt(value.substring(4, 6));
        if (year == 0) {
            return FeedbackCode.YEAR_IN_ERA_ZERO;
        }
        if (month < 1 || month > 12) {
            return FeedbackCode.INVALID_MONTH;
        }
        // CEEDAYS counts days from the start of the Lillian calendar (15 October 1582).
        if (year < 1582) {
            return FeedbackCode.UNSUPPORTED_RANGE;
        }
        return parseDate(value) == null ? FeedbackCode.BAD_DATE_VALUE : FeedbackCode.VALID;
    }

    /** COBOL paragraph: EDIT-YEAR-CCYY. */
    private void editYearCcyy(EditState state) {
        state.yearFlag = DateValidationResult.FLAG_NOT_OK;
        String ccyy = state.ccyymmdd.substring(0, 4);

        if (isBlank(ccyy)) {
            state.inputError = true;
            state.yearFlag = DateValidationResult.FLAG_BLANK;
            state.message(state.variableName + " : Year must be supplied.");
            return;
        }
        if (!isNumeric(ccyy)) {
            state.inputError = true;
            state.yearFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName + " must be 4 digit number.");
            return;
        }
        int century = Integer.parseInt(ccyy.substring(0, 2));
        if (century != 20 && century != 19) {
            state.inputError = true;
            state.yearFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName + " : Century is not valid.");
            return;
        }
        state.yearFlag = DateValidationResult.FLAG_VALID;
    }

    /** COBOL paragraph: EDIT-MONTH. */
    private void editMonth(EditState state) {
        state.monthFlag = DateValidationResult.FLAG_NOT_OK;
        String mm = state.ccyymmdd.substring(4, 6);

        if (isBlank(mm)) {
            state.inputError = true;
            state.monthFlag = DateValidationResult.FLAG_BLANK;
            state.message(state.variableName + " : Month must be supplied.");
            return;
        }
        if (!isNumeric(mm) || Integer.parseInt(mm) < 1 || Integer.parseInt(mm) > 12) {
            state.inputError = true;
            state.monthFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName + ": Month must be a number between 1 and 12.");
            return;
        }
        state.monthFlag = DateValidationResult.FLAG_VALID;
    }

    /** COBOL paragraph: EDIT-DAY. */
    private void editDay(EditState state) {
        state.dayFlag = DateValidationResult.FLAG_VALID;
        String dd = state.ccyymmdd.substring(6, 8);

        if (isBlank(dd)) {
            state.inputError = true;
            state.dayFlag = DateValidationResult.FLAG_BLANK;
            state.message(state.variableName + " : Day must be supplied.");
            return;
        }
        if (!isNumeric(dd) || Integer.parseInt(dd) < 1 || Integer.parseInt(dd) > 31) {
            state.inputError = true;
            state.dayFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName + ":day must be a number between 1 and 31.");
            return;
        }
        state.dayFlag = DateValidationResult.FLAG_VALID;
    }

    /**
     * COBOL paragraph: EDIT-DAY-MONTH-YEAR.
     *
     * @return true when the paragraph branched to EDIT-DATE-CCYYMMDD-EXIT, skipping EDIT-DATE-LE
     */
    private boolean editDayMonthYear(EditState state) {
        String mm = state.ccyymmdd.substring(4, 6);
        String dd = state.ccyymmdd.substring(6, 8);
        if (!isNumeric(mm) || !isNumeric(dd)) {
            return true;
        }
        int month = Integer.parseInt(mm);
        int day = Integer.parseInt(dd);
        boolean monthWith31Days = month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month == 10 || month == 12;

        if (!monthWith31Days && day == 31) {
            state.inputError = true;
            state.dayFlag = DateValidationResult.FLAG_NOT_OK;
            state.monthFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName + ":Cannot have 31 days in this month.");
            return true;
        }
        if (month == 2 && day == 30) {
            state.inputError = true;
            state.dayFlag = DateValidationResult.FLAG_NOT_OK;
            state.monthFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName + ":Cannot have 30 days in this month.");
            return true;
        }
        if (month == 2 && day == 29) {
            String ccyy = state.ccyymmdd.substring(0, 4);
            if (!isNumeric(ccyy)) {
                return true;
            }
            int year = Integer.parseInt(ccyy);
            // WS-EDIT-DATE-YY-N = 0 means a century year, which must be divisible by 400.
            int divisor = year % 100 == 0 ? 400 : 4;
            if (year % divisor != 0) {
                state.inputError = true;
                state.dayFlag = DateValidationResult.FLAG_NOT_OK;
                state.monthFlag = DateValidationResult.FLAG_NOT_OK;
                state.yearFlag = DateValidationResult.FLAG_NOT_OK;
                state.message(state.variableName
                        + ":Not a leap year.Cannot have 29 days in this month.");
                return true;
            }
        }
        return !state.flagsValid();
    }

    /** COBOL paragraph: EDIT-DATE-LE. */
    private void editDateLanguageEnvironment(EditState state) {
        LanguageEnvironmentDateResult le =
                validateWithLanguageEnvironment(state.ccyymmdd, FORMAT_CCYYMMDD);
        if (le.severity() != 0) {
            state.inputError = true;
            state.dayFlag = DateValidationResult.FLAG_NOT_OK;
            state.monthFlag = DateValidationResult.FLAG_NOT_OK;
            state.yearFlag = DateValidationResult.FLAG_NOT_OK;
            state.message(state.variableName
                    + " validation error Sev code: "
                    + "%04d".formatted(le.severity())
                    + " Message code: "
                    + "%04d".formatted(le.messageNumber()));
            return;
        }
        if (!state.inputError) {
            state.dayFlag = DateValidationResult.FLAG_VALID;
        }
    }

    private static LocalDate parseDate(String ccyymmdd) {
        if (!isNumeric(ccyymmdd) || ccyymmdd.length() != 8) {
            return null;
        }
        try {
            return LocalDate.of(
                    Integer.parseInt(ccyymmdd.substring(0, 4)),
                    Integer.parseInt(ccyymmdd.substring(4, 6)),
                    Integer.parseInt(ccyymmdd.substring(6, 8)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static boolean isNumeric(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** {@code EQUAL LOW-VALUES OR EQUAL SPACES}. */
    private static boolean isBlank(String value) {
        return value.isBlank() || value.chars().allMatch(c -> c == 0);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String pad(String value, int length) {
        String text = value == null ? "" : value;
        return text.length() >= length ? text.substring(0, length) : padRight(text, length);
    }

    private static String padRight(String value, int length) {
        return String.format("%-" + length + "s", value);
    }

    /** Mutable counterpart of the CSUTLDWY working storage during one edit run. */
    private static final class EditState {
        private final String ccyymmdd;
        private final String variableName;
        private boolean inputError;
        private String returnMessage = "";
        private char yearFlag = DateValidationResult.FLAG_NOT_OK;
        private char monthFlag = DateValidationResult.FLAG_NOT_OK;
        private char dayFlag = DateValidationResult.FLAG_NOT_OK;

        private EditState(String ccyymmdd, String variableName) {
            this.ccyymmdd = ccyymmdd;
            this.variableName = variableName;
        }

        /** {@code IF WS-RETURN-MSG-OFF} — only the first message is kept. */
        private void message(String text) {
            if (returnMessage.isEmpty()) {
                returnMessage = text;
            }
        }

        private boolean flagsValid() {
            return yearFlag == DateValidationResult.FLAG_VALID
                    && monthFlag == DateValidationResult.FLAG_VALID
                    && dayFlag == DateValidationResult.FLAG_VALID;
        }

        private DateValidationResult result(boolean reachedDateLeExit) {
            boolean flagsValid = reachedDateLeExit || flagsValid();
            return new DateValidationResult(
                    inputError, flagsValid, returnMessage, yearFlag, monthFlag, dayFlag);
        }
    }
}
