package com.carddemo.batch.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * COBOL display-numeric helpers.
 *
 * <p>Signed zoned fields ({@code PIC S9(n)V99}) use the trailing overpunch sign found in
 * {@code app/data/ASCII}: {@code '{'} and {@code 'A'..'I'} for +0..+9, {@code '}'} and
 * {@code 'J'..'R'} for -0..-9. Parsing also accepts GnuCOBOL ASCII signs ({@code 'p'..'y'}) and
 * separate leading/trailing {@code '+'/'-'}. Storing into a field follows COBOL MOVE semantics:
 * excess decimals and high-order digits are truncated, never rounded.
 */
public final class CobolDecimal {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";
    private static final Pattern REPEAT = Pattern.compile("(.)\\((\\d+)\\)");

    private CobolDecimal() {
    }

    /** Parses a {@code PIC S9(n)V9(scale)} display field. Blank fields parse as zero. */
    public static BigDecimal parseSigned(String field, int scale) {
        if (field == null || field.isBlank()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        String digits = field;
        boolean negative = false;
        char first = digits.charAt(0);
        char last = digits.charAt(digits.length() - 1);
        if (first == '+' || first == '-') {
            negative = first == '-';
            digits = digits.substring(1);
        } else if (last == '+' || last == '-') {
            negative = last == '-';
            digits = digits.substring(0, digits.length() - 1);
        } else if (!Character.isDigit(last)) {
            int positive = POSITIVE_OVERPUNCH.indexOf(last);
            int negativeDigit = NEGATIVE_OVERPUNCH.indexOf(last);
            int digit;
            if (positive >= 0) {
                digit = positive;
            } else if (negativeDigit >= 0) {
                digit = negativeDigit;
                negative = true;
            } else if (last >= 'p' && last <= 'y') {
                digit = last - 'p';
                negative = true;
            } else {
                throw new NumberFormatException("Invalid zoned decimal: '" + field + "'");
            }
            digits = digits.substring(0, digits.length() - 1) + (char) ('0' + digit);
        }
        digits = digits.replace(' ', '0');
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new NumberFormatException("Invalid zoned decimal: '" + field + "'");
        }
        BigDecimal value = new BigDecimal(new BigInteger(digits), scale);
        return negative ? value.negate() : value;
    }

    /** Formats a value as a {@code PIC S9(integerDigits)V9(scale)} field with trailing overpunch sign. */
    public static String formatSigned(BigDecimal value, int integerDigits, int scale) {
        BigDecimal stored = toField(value, integerDigits, scale);
        String digits = magnitudeDigits(stored, integerDigits + scale);
        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        String signs = stored.signum() < 0 ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH;
        return digits.substring(0, digits.length() - 1) + signs.charAt(lastDigit);
    }

    /** Parses a {@code PIC 9(n)} display field. Blank fields parse as zero. */
    public static long parseUnsigned(String field) {
        return parseSigned(field, 0).abs().longValueExact();
    }

    /** Parses a {@code PIC 9(n)V9(scale)} display field. */
    public static BigDecimal parseUnsigned(String field, int scale) {
        return parseSigned(field, scale).abs();
    }

    /** Formats a value as a zero-padded {@code PIC 9(digits)} field (sign dropped, high-order truncated). */
    public static String formatUnsigned(long value, int digits) {
        return formatUnsigned(BigDecimal.valueOf(value), digits, 0);
    }

    /** Formats a value as a zero-padded {@code PIC 9(integerDigits)V9(scale)} field. */
    public static String formatUnsigned(BigDecimal value, int integerDigits, int scale) {
        return magnitudeDigits(toField(value, integerDigits, scale), integerDigits + scale);
    }

    /** Truncates excess decimals (COBOL arithmetic without ROUNDED). */
    public static BigDecimal truncate(BigDecimal value, int scale) {
        return (value == null ? BigDecimal.ZERO : value).setScale(scale, RoundingMode.DOWN);
    }

    /**
     * Returns the value a {@code PIC S9(integerDigits)V9(scale)} field holds after a MOVE/ADD of
     * {@code value}: decimals and high-order digits truncated, null treated as zero.
     */
    public static BigDecimal toField(BigDecimal value, int integerDigits, int scale) {
        BigDecimal truncated = truncate(value, scale);
        BigInteger modulus = BigInteger.TEN.pow(integerDigits + scale);
        BigInteger unscaled = truncated.unscaledValue();
        BigInteger magnitude = unscaled.abs().mod(modulus);
        return new BigDecimal(unscaled.signum() < 0 ? magnitude.negate() : magnitude, scale);
    }

    /** Returns the value a {@code PIC 9(digits)} field holds after a MOVE of {@code value}. */
    public static long toUnsignedField(long value, int digits) {
        return toField(BigDecimal.valueOf(value), digits, 0).abs().longValueExact();
    }

    /**
     * Formats a value with a numeric-edited picture made of an optional fixed {@code '+'}/{@code '-'}
     * sign (first or last position), {@code Z}, {@code 9}, {@code ','} and {@code '.'}, e.g.
     * {@code -ZZZ,ZZZ,ZZZ.ZZ} or {@code +ZZZ,ZZZ,ZZZ.ZZ}.
     *
     * <ul>
     *   <li>{@code '-'} prints '-' for negative values and a space otherwise; {@code '+'} prints '+'
     *       or '-'.</li>
     *   <li>Leading zeros and their commas are replaced by spaces up to the first non-zero digit or
     *       the decimal point.</li>
     *   <li>If every digit position is {@code Z} and the value is zero, the whole item (sign
     *       included) is spaces.</li>
     * </ul>
     */
    public static String formatEdited(BigDecimal value, String picture) {
        String pic = expandPicture(picture);
        int point = pic.indexOf('.');
        int integerDigits = 0;
        int fractionDigits = 0;
        for (int i = 0; i < pic.length(); i++) {
            char c = pic.charAt(i);
            if (c == 'Z' || c == '9') {
                if (point >= 0 && i > point) {
                    fractionDigits++;
                } else {
                    integerDigits++;
                }
            } else if (c != ',' && c != '.' && !((c == '+' || c == '-') && (i == 0 || i == pic.length() - 1))) {
                throw new IllegalArgumentException("Unsupported edited picture: " + picture);
            }
        }
        BigDecimal stored = toField(value, integerDigits, fractionDigits);
        String digits = magnitudeDigits(stored, integerDigits + fractionDigits);
        if (pic.indexOf('9') < 0 && stored.signum() == 0) {
            return " ".repeat(pic.length());
        }
        boolean negative = stored.signum() < 0;
        StringBuilder out = new StringBuilder(pic.length());
        boolean suppressing = true;
        int next = 0;
        for (int i = 0; i < pic.length(); i++) {
            char c = pic.charAt(i);
            switch (c) {
                case '+':
                    out.append(negative ? '-' : '+');
                    break;
                case '-':
                    out.append(negative ? '-' : ' ');
                    break;
                case '.':
                    suppressing = false;
                    out.append('.');
                    break;
                case ',':
                    out.append(suppressing ? ' ' : ',');
                    break;
                case '9':
                    suppressing = false;
                    out.append(digits.charAt(next++));
                    break;
                default: {
                    char d = digits.charAt(next++);
                    if (suppressing && d == '0') {
                        out.append(' ');
                    } else {
                        suppressing = false;
                        out.append(d);
                    }
                }
            }
        }
        return out.toString();
    }

    /** Parses text produced by {@link #formatEdited}; blank text parses as zero. */
    public static BigDecimal parseEdited(String text, int scale) {
        String cleaned = text.replace(",", "").replace(" ", "");
        boolean negative = cleaned.contains("-");
        cleaned = cleaned.replace("-", "").replace("+", "");
        if (cleaned.isEmpty() || cleaned.equals(".")) {
            return BigDecimal.ZERO.setScale(scale);
        }
        BigDecimal value = new BigDecimal(cleaned).setScale(scale, RoundingMode.DOWN);
        return negative ? value.negate() : value;
    }

    /**
     * Port of {@code Z-GET-DB2-FORMAT-TIMESTAMP}: {@code FUNCTION CURRENT-DATE} reformatted as
     * {@code YYYY-MM-DD-HH.MM.SS.hh0000} (hundredths of a second followed by '0000').
     */
    public static String db2Timestamp(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return String.format("%04d-%02d-%02d-%02d.%02d.%02d.%02d0000",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond(), now.getNano() / 10_000_000);
    }

    private static String magnitudeDigits(BigDecimal stored, int totalDigits) {
        String digits = stored.unscaledValue().abs().toString();
        return "0".repeat(Math.max(0, totalDigits - digits.length())) + digits;
    }

    private static String expandPicture(String picture) {
        Matcher m = REPEAT.matcher(picture);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1).repeat(Integer.parseInt(m.group(2)))));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
