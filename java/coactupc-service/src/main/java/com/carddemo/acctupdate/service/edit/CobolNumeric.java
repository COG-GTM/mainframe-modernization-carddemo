package com.carddemo.acctupdate.service.edit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CobolNumeric {
    private static final Pattern NUMBER = Pattern.compile("^([+-]?)(\\$?)([0-9][0-9,]*)(?:\\.([0-9]+))?([+-]|CR|DB)?$");
    private CobolNumeric() {}
    public static boolean testNumvalC(String text) { try { numvalC(text); return true; } catch (RuntimeException e) { return false; } }
    public static BigDecimal numvalC(String text) {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException();
        Matcher m = NUMBER.matcher(text.trim().toUpperCase());
        if (!m.matches()) throw new IllegalArgumentException(text);
        String integer = m.group(3).replace(",", "");
        if (integer.length() > 10) throw new IllegalArgumentException(text);
        String sign = m.group(1);
        String trailing = m.group(5);
        if (trailing != null && !sign.isEmpty()) throw new IllegalArgumentException(text);
        if ("-".equals(trailing) || "DB".equals(trailing)) sign = "-";
        BigDecimal result = new BigDecimal(sign + integer + (m.group(4) == null ? "" : "." + m.group(4)));
        return result.setScale(2, RoundingMode.DOWN);
    }
}
