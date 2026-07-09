package com.carddemo.util;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Reusable date-validation component &mdash; the Java translation of the COBOL program
 * {@code CSUTLDTC} (<em>"CALL TO CEEDAYS"</em>).
 *
 * <p>{@code CSUTLDTC} takes a date string and a format/picture string, calls the Language
 * Environment callable service {@code CEEDAYS} (which parses the string against the picture,
 * verifies it is a real calendar date and converts it to a Lilian day number), and maps the
 * {@code CEEDAYS} feedback code to a severity, a message number and a 15-character result text.
 * This class reproduces that behaviour without any mainframe runtime: it parses the picture,
 * checks the field values against the Gregorian calendar (month {@code 1&ndash;12}, day valid for
 * the month including leap years) and returns the same {@link DateValidationStatus feedback codes}.</p>
 *
 * <p>It is deliberately framework-light: the validation methods are pure and depend only on the
 * JDK, so the class can be used as a plain object or as the injected Spring {@link Component} it
 * is annotated as. The supported picture tokens are {@code YYYY} / {@code YY} (year),
 * {@code MM} (month) and {@code DD} (day); any other run of letters, or a {@code Y} run that is
 * not 2 or 4 long, makes the picture invalid ({@link DateValidationStatus#BAD_PICTURE_STRING}).
 * Non-alphanumeric characters in the picture are treated as literal separators (so
 * {@code YYYY-MM-DD}, {@code MM/DD/YYYY} etc. are all accepted).</p>
 *
 * <p><strong>Reuse:</strong> later migration waves that currently in-line their own date checks
 * (the {@code EDIT-DATE-CCYYMMDD} family of paragraphs in the {@code CSUTLDPY} copybook) are
 * expected to refactor those checks to call this component. Note that the caller-side
 * {@code EDIT-YEAR-CCYY} paragraph additionally rejects centuries other than {@code 19}/{@code 20}
 * (years outside {@code 1900&ndash;2099}); that business restriction is <em>not</em> part of
 * {@code CSUTLDTC}/{@code CEEDAYS} and so is not enforced here.</p>
 */
@Component
public class DateValidator {

    /** The default picture used by CardDemo callers ({@code CSUTLDPY} moves {@code 'YYYYMMDD'}). */
    public static final String DEFAULT_FORMAT = "YYYYMMDD";

    /**
     * The earliest date {@code CEEDAYS} supports: Lilian day {@code 1} is {@code 1582-10-15}
     * (the first day of the Gregorian calendar). Earlier dates yield
     * {@link DateValidationStatus#UNSUPPORTED_RANGE}.
     */
    private static final LocalDate LILIAN_EPOCH = LocalDate.of(1582, 10, 15);

    /** Day 0 in Lilian terms; used to convert an epoch day into a Lilian day number. */
    private static final long LILIAN_EPOCH_DAY = LILIAN_EPOCH.toEpochDay() - 1L;

    /** The latest date {@code CEEDAYS} supports. */
    private static final LocalDate MAX_SUPPORTED = LocalDate.of(9999, 12, 31);

    /**
     * Validate {@code date} against the default {@code YYYYMMDD} picture.
     *
     * @param date the date string to validate (may be {@code null})
     * @return the validation result, mirroring {@code CSUTLDTC}
     */
    public DateValidationResult validate(String date) {
        return validate(date, DEFAULT_FORMAT);
    }

    /**
     * Validate {@code date} against {@code format}, reproducing {@code CSUTLDTC}.
     *
     * @param date   the date string to validate ({@code LS-DATE}); may be {@code null}
     * @param format the format/picture string ({@code LS-DATE-FORMAT}); may be {@code null}
     * @return the validation result: status, severity, message number, result text, the Lilian
     *         day number (when valid) and the 80-character {@code LS-RESULT} message
     */
    public DateValidationResult validate(String date, String format) {
        String rawDate = date == null ? "" : date;
        String rawFormat = format == null ? "" : format;

        // CEEDAYS ignores trailing blanks in both the picture and the input (CardDemo passes
        // 10-byte fields holding e.g. "YYYYMMDD  " / "20240229  ").
        String pic = stripTrailing(rawFormat);
        String value = stripTrailing(rawDate);

        Outcome outcome = evaluate(pic, value);
        return new DateValidationResult(outcome.status, rawDate, rawFormat, outcome.lilianDay,
                formatMessage(outcome.status, rawDate, rawFormat));
    }

    /** Convenience predicate: {@code true} iff the date is valid under the default picture. */
    public boolean isValid(String date) {
        return validate(date).isValid();
    }

    /** Convenience predicate: {@code true} iff the date is valid under {@code format}. */
    public boolean isValid(String date, String format) {
        return validate(date, format).isValid();
    }

    private Outcome evaluate(String pic, String value) {
        Picture picture = Picture.parse(pic);
        if (picture == null) {
            return Outcome.of(DateValidationStatus.BAD_PICTURE_STRING);
        }

        Fields fields = new Fields();
        int di = 0;
        for (Token token : picture.tokens) {
            if (token.literal != 0) {
                if (di >= value.length()) {
                    return Outcome.of(DateValidationStatus.INSUFFICIENT_DATA);
                }
                // A separator that does not line up is treated as a bad date value.
                if (value.charAt(di) != token.literal) {
                    return Outcome.of(DateValidationStatus.BAD_DATE_VALUE);
                }
                di++;
                continue;
            }

            if (di + token.width > value.length()) {
                return Outcome.of(DateValidationStatus.INSUFFICIENT_DATA);
            }
            String chunk = value.substring(di, di + token.width);
            di += token.width;
            if (!isAllDigits(chunk)) {
                return Outcome.of(DateValidationStatus.NONNUMERIC_DATA);
            }
            fields.apply(token.field, Integer.parseInt(chunk));
        }

        int year = fields.resolveYear();
        int month = fields.month;
        int day = fields.day;

        // Year of zero has no meaning in an era (CEEDAYS treats 0000 as "year in era is 0").
        if (year == 0) {
            return Outcome.of(DateValidationStatus.YEAR_IN_ERA_ZERO);
        }
        if (month < 1 || month > 12) {
            return Outcome.of(DateValidationStatus.INVALID_MONTH);
        }
        if (day < 1 || day > daysInMonth(month, year)) {
            return Outcome.of(DateValidationStatus.BAD_DATE_VALUE);
        }

        LocalDate resolved = LocalDate.of(year, month, day);
        if (resolved.isBefore(LILIAN_EPOCH) || resolved.isAfter(MAX_SUPPORTED)) {
            return Outcome.of(DateValidationStatus.UNSUPPORTED_RANGE);
        }
        return new Outcome(DateValidationStatus.VALID, resolved.toEpochDay() - LILIAN_EPOCH_DAY);
    }

    private static int daysInMonth(int month, int year) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return isLeapYear(year) ? 29 : 28;
            default:
                return 0;
        }
    }

    /**
     * Gregorian leap-year rule, matching the {@code EDIT-DAY-MONTH-YEAR} logic in {@code CSUTLDPY}
     * (divide by 400 for a century year, otherwise by 4).
     */
    private static boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            return year % 400 == 0;
        }
        return year % 4 == 0;
    }

    private static boolean isAllDigits(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static String stripTrailing(String s) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == ' ') {
            end--;
        }
        return s.substring(0, end);
    }

    /**
     * Build the 80-character {@code WS-MESSAGE} layout returned in {@code LS-RESULT}:
     * <pre>severity(4) + "Mesg Code: "(11) + msgNo(4) + " " + result(15) + " "
     *      + "TstDate: "(9) + date(10) + " " + "Mask used:"(10) + fmt(10) + " " + "   "</pre>
     */
    private static String formatMessage(DateValidationStatus status, String date, String format) {
        StringBuilder sb = new StringBuilder(80);
        sb.append(zeroPad4(status.severity()));
        sb.append(fixed("Mesg Code:", 11));
        sb.append(zeroPad4(status.messageNumber()));
        sb.append(' ');
        sb.append(fixed(status.resultText(), 15));
        sb.append(' ');
        sb.append(fixed("TstDate:", 9));
        sb.append(fixed(date, 10));
        sb.append(' ');
        sb.append(fixed("Mask used:", 10));
        sb.append(fixed(format, 10));
        sb.append(' ');
        sb.append("   ");
        return sb.toString();
    }

    private static String zeroPad4(int n) {
        String s = Integer.toString(Math.abs(n) % 10000);
        return "0000".substring(s.length()) + s;
    }

    /** Left-justify to {@code width}, padding with spaces or truncating (COBOL {@code PIC X(n)}). */
    private static String fixed(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) {
            return v.substring(0, width);
        }
        StringBuilder sb = new StringBuilder(width).append(v);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /** Parsed picture: an ordered list of field/literal tokens. */
    private static final class Picture {
        final Token[] tokens;

        private Picture(Token[] tokens) {
            this.tokens = tokens;
        }

        /** @return the parsed picture, or {@code null} if the picture string is invalid. */
        static Picture parse(String pic) {
            if (pic.isEmpty()) {
                return null;
            }
            java.util.List<Token> tokens = new java.util.ArrayList<>();
            boolean hasField = false;
            int i = 0;
            while (i < pic.length()) {
                char c = pic.charAt(i);
                if (isLetter(c)) {
                    int j = i;
                    while (j < pic.length() && pic.charAt(j) == c) {
                        j++;
                    }
                    int run = j - i;
                    Token token = fieldToken(c, run);
                    if (token == null) {
                        return null;
                    }
                    tokens.add(token);
                    hasField = true;
                    i = j;
                } else {
                    tokens.add(Token.literal(c));
                    i++;
                }
            }
            return hasField ? new Picture(tokens.toArray(new Token[0])) : null;
        }

        private static Token fieldToken(char c, int run) {
            switch (Character.toUpperCase(c)) {
                case 'Y':
                    if (run == 4) {
                        return Token.field(Field.YEAR4, 4);
                    }
                    if (run == 2) {
                        return Token.field(Field.YEAR2, 2);
                    }
                    return null;
                case 'M':
                    return run == 2 ? Token.field(Field.MONTH, 2) : null;
                case 'D':
                    return run == 2 ? Token.field(Field.DAY, 2) : null;
                default:
                    return null;
            }
        }

        private static boolean isLetter(char c) {
            return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
        }
    }

    private enum Field { YEAR4, YEAR2, MONTH, DAY }

    /** A picture token: either a numeric field of a given width, or a single literal character. */
    private static final class Token {
        final Field field;
        final int width;
        final char literal;

        private Token(Field field, int width, char literal) {
            this.field = field;
            this.width = width;
            this.literal = literal;
        }

        static Token field(Field field, int width) {
            return new Token(field, width, (char) 0);
        }

        static Token literal(char c) {
            return new Token(null, 0, c);
        }
    }

    /** Mutable accumulator for the parsed year/month/day field values. */
    private static final class Fields {
        Integer year4;
        Integer year2;
        int month = 1;
        int day = 1;

        void apply(Field field, int value) {
            switch (field) {
                case YEAR4 -> year4 = value;
                case YEAR2 -> year2 = value;
                case MONTH -> month = value;
                case DAY -> day = value;
                default -> { }
            }
        }

        /** Full 4-digit year; a 2-digit year defaults to the {@code 1900}s (CEEDAYS window). */
        int resolveYear() {
            if (year4 != null) {
                return year4;
            }
            if (year2 != null) {
                return 1900 + year2;
            }
            return 0;
        }
    }

    /** Internal validation outcome bundling the status with the (optional) Lilian day number. */
    private static final class Outcome {
        final DateValidationStatus status;
        final Long lilianDay;

        Outcome(DateValidationStatus status, Long lilianDay) {
            this.status = status;
            this.lilianDay = lilianDay;
        }

        static Outcome of(DateValidationStatus status) {
            return new Outcome(status, null);
        }
    }
}
