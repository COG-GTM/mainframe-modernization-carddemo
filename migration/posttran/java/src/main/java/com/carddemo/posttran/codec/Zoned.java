package com.carddemo.posttran.codec;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Signed zoned decimal ("sign overpunch") codec for COBOL {@code PIC S9(n)V99 DISPLAY} fields.
 *
 * <p>The CardDemo drop stores the sign in the low-order byte using the IBM convention that
 * survives an EBCDIC-to-ASCII translation as {@code {A..I} / }J..R}: {@code '{'} is +0,
 * {@code 'A'} is +1 ... {@code '}'} is -0, {@code 'J'} is -1. The legacy program must therefore
 * be compiled with {@code cobc -fsign=EBCDIC}; with GnuCOBOL's native ASCII sign convention the
 * overpunch is not recognised and every negative amount silently reads as positive
 * (see migration/posttran/README.md).
 *
 * <p>A trailing plain digit is accepted on input and means "positive, unsigned representation";
 * output is always written in overpunch form, which is what the legacy program emits for every
 * field it touches.
 */
public final class Zoned {

    private static final String POSITIVE = "{ABCDEFGHI";
    private static final String NEGATIVE = "}JKLMNOPQR";

    private Zoned() {
    }

    /** Decodes {@code digits} characters of zoned decimal into a value with the given scale. */
    public static BigDecimal decode(String field, int scale) {
        if (field.isEmpty()) {
            throw new IllegalArgumentException("empty zoned field");
        }
        String head = field.substring(0, field.length() - 1);
        char last = field.charAt(field.length() - 1);

        int lastDigit;
        boolean negative;
        int posIndex = POSITIVE.indexOf(last);
        int negIndex = NEGATIVE.indexOf(last);
        if (posIndex >= 0) {
            lastDigit = posIndex;
            negative = false;
        } else if (negIndex >= 0) {
            lastDigit = negIndex;
            negative = true;
        } else if (last >= '0' && last <= '9') {
            lastDigit = last - '0';
            negative = false;
        } else {
            throw new IllegalArgumentException("not a zoned decimal field: '" + field + "'");
        }

        String normalisedHead = head.isEmpty() ? "" : head.replace(' ', '0');
        BigInteger unscaled = new BigInteger((normalisedHead.isEmpty() ? "0" : normalisedHead) + lastDigit);
        if (negative) {
            unscaled = unscaled.negate();
        }
        return new BigDecimal(unscaled, scale);
    }

    /** Encodes a value into {@code digits} characters of zoned decimal with a trailing overpunch. */
    public static String encode(BigDecimal value, int digits, int scale) {
        BigDecimal scaled = value.setScale(scale, java.math.RoundingMode.UNNECESSARY);
        String body = scaled.unscaledValue().abs().toString();
        if (body.length() > digits) {
            // COBOL truncates high-order digits on overflow rather than failing.
            body = body.substring(body.length() - digits);
        }
        body = "0".repeat(digits - body.length()) + body;

        boolean negative = scaled.signum() < 0;
        char overpunch = (negative ? NEGATIVE : POSITIVE).charAt(body.charAt(digits - 1) - '0');
        return body.substring(0, digits - 1) + overpunch;
    }
}
