package com.carddemo.acctupdate.service.edit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CobolNumeric {
    private static final Pattern NUMBER = Pattern.compile(
        "^([+-]?)(\\$?)([0-9][0-9,]*)(?:\\.([0-9]+))?([+-]|CR|DB)?$"
    );

    private CobolNumeric() {
    }

    public static boolean testNumvalC(String text) {
        try {
            numvalC(text);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static BigDecimal numvalC(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }

        Matcher matcher = NUMBER.matcher(text.trim().toUpperCase());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(text);
        }

        String integer = matcher.group(3).replace(",", "");
        if (integer.length() > 10) {
            throw new IllegalArgumentException(text);
        }

        String sign = matcher.group(1);
        String trailing = matcher.group(5);
        if (trailing != null && !sign.isEmpty()) {
            throw new IllegalArgumentException(text);
        }
        if ("-".equals(trailing) || "DB".equals(trailing)) {
            sign = "-";
        }

        String fraction = matcher.group(4) == null ? "" : "." + matcher.group(4);
        BigDecimal result = new BigDecimal(sign + integer + fraction);
        return result.setScale(2, RoundingMode.DOWN);
    }
}
