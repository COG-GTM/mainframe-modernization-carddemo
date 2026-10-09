package com.carddemo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;

/**
 * Java replacement for {@code CSUTLDTC}, which validated a date string against a picture mask by
 * calling the Language Environment service {@code CEEDAYS}.
 *
 * <p>The severity/result pairs mirror the feedback codes the COBOL routine translated.
 */
public final class CobolDateValidator {

    private static final Map<String, String> MASKS = Map.of(
            "YYYY-MM-DD", "uuuu-MM-dd",
            "YYYYMMDD", "uuuuMMdd",
            "MM/DD/YYYY", "MM/dd/uuuu",
            "DD/MM/YYYY", "dd/MM/uuuu");

    private CobolDateValidator() {
    }

    /** Result of a validation, mirroring the {@code LS-RESULT} / {@code RETURN-CODE} pair. */
    public record Result(int severity, String message, LocalDate date) {

        public boolean isValid() {
            return severity == 0;
        }
    }

    public static Result validate(String date, String format) {
        String mask = MASKS.get(format == null ? "" : format.trim().toUpperCase());
        if (mask == null) {
            return new Result(12, "Bad Pic String", null);
        }
        String value = date == null ? "" : date.trim();
        if (value.isEmpty()) {
            return new Result(12, "Insufficient", null);
        }
        if (value.length() != format.trim().length()) {
            return new Result(12, "Insufficient", null);
        }
        try {
            LocalDate parsed = LocalDate.parse(
                    value, DateTimeFormatter.ofPattern(mask).withResolverStyle(ResolverStyle.STRICT));
            return new Result(0, "Date is valid", parsed);
        } catch (DateTimeParseException ex) {
            if (!value.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '/')) {
                return new Result(12, "Nonnumeric data", null);
            }
            return new Result(12, "Date is invalid", null);
        }
    }

    /** Convenience check for the {@code YYYY-MM-DD} fields stored throughout CardDemo. */
    public static boolean isValidIsoDate(String date) {
        return validate(date, "YYYY-MM-DD").isValid();
    }
}
