package com.carddemo.util;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.JulianFields;
import java.util.Map;

/**
 * Date conversion service replacing the COBOL utility program {@code CSUTLDTC.cbl}.
 *
 * <p>The original COBOL program calls {@code CEEDAYS} (Language Environment callable
 * service) to convert dates between formats and validate them. This service provides
 * equivalent functionality using the {@link java.time} API.</p>
 *
 * <h3>Supported Formats</h3>
 * <ul>
 *   <li>{@code YYYYMMDD} — ISO basic date (e.g. {@code 20240115})</li>
 *   <li>{@code YYYY-MM-DD} — ISO extended date (e.g. {@code 2024-01-15})</li>
 *   <li>{@code MM/DD/YYYY} — US date format (e.g. {@code 01/15/2024})</li>
 *   <li>{@code DD/MM/YYYY} — European date format (e.g. {@code 15/01/2024})</li>
 *   <li>{@code JULIAN} — Julian day number (days since epoch)</li>
 * </ul>
 */
@Service
public class DateConversionService {

    /** Format identifier for Julian day numbers. */
    public static final String JULIAN = "JULIAN";

    private static final Map<String, DateTimeFormatter> FORMATTERS = Map.of(
            "YYYYMMDD", DateTimeFormatter.ofPattern("uuuuMMdd")
                    .withResolverStyle(ResolverStyle.STRICT),
            "YYYY-MM-DD", DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT),
            "MM/DD/YYYY", DateTimeFormatter.ofPattern("MM/dd/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT),
            "DD/MM/YYYY", DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT)
    );

    /**
     * Converts a date string from one format to another.
     *
     * <p>This method replaces the COBOL {@code CALL 'CSUTLDTC'} pattern used
     * throughout the CardDemo application for date format conversion.</p>
     *
     * @param input        the date string to convert
     * @param inputFormat  the format of the input string (see class-level docs for supported formats)
     * @param outputFormat the desired output format
     * @return the date formatted in the output format
     * @throws IllegalArgumentException if a format identifier is not recognized
     * @throws DateTimeParseException   if the input string cannot be parsed with the given format
     */
    public String convertDate(String input, String inputFormat, String outputFormat) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input date must not be null or blank");
        }
        if (inputFormat == null || outputFormat == null) {
            throw new IllegalArgumentException("Input and output formats must not be null");
        }

        LocalDate date = parseDate(input.trim(), inputFormat.trim().toUpperCase());
        return formatDate(date, outputFormat.trim().toUpperCase());
    }

    /**
     * Validates whether a date string conforms to the given format.
     *
     * <p>Mirrors the validation behavior of {@code CSUTLDTC.cbl}, which returns a
     * severity code of 0 for valid dates.</p>
     *
     * @param input  the date string to validate
     * @param format the expected format of the date string
     * @return {@code true} if the input is a valid date in the specified format
     */
    public boolean isValidDate(String input, String format) {
        try {
            parseDate(input.trim(), format.trim().toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts a date string to a Julian day number.
     *
     * @param input       the date string
     * @param inputFormat the format of the input string
     * @return the Julian day number
     */
    public long toJulianDay(String input, String inputFormat) {
        LocalDate date = parseDate(input.trim(), inputFormat.trim().toUpperCase());
        return date.getLong(JulianFields.JULIAN_DAY);
    }

    /**
     * Converts a Julian day number to a date string in the specified format.
     *
     * @param julianDay    the Julian day number
     * @param outputFormat the desired output format
     * @return the formatted date string
     */
    public String fromJulianDay(long julianDay, String outputFormat) {
        LocalDate date = LocalDate.MIN.with(JulianFields.JULIAN_DAY, julianDay);
        return formatDate(date, outputFormat.trim().toUpperCase());
    }

    private LocalDate parseDate(String input, String format) {
        if (JULIAN.equals(format)) {
            long julianDay = Long.parseLong(input);
            return LocalDate.MIN.with(JulianFields.JULIAN_DAY, julianDay);
        }

        DateTimeFormatter formatter = FORMATTERS.get(format);
        if (formatter == null) {
            throw new IllegalArgumentException("Unsupported date format: " + format);
        }
        return LocalDate.parse(input, formatter);
    }

    private String formatDate(LocalDate date, String format) {
        if (JULIAN.equals(format)) {
            return String.valueOf(date.getLong(JulianFields.JULIAN_DAY));
        }

        DateTimeFormatter formatter = FORMATTERS.get(format);
        if (formatter == null) {
            throw new IllegalArgumentException("Unsupported date format: " + format);
        }
        return date.format(formatter);
    }
}
