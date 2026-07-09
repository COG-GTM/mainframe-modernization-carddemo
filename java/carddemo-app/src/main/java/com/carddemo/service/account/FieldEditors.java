package com.carddemo.service.account;

import java.math.BigDecimal;

/**
 * Reusable field-level edit routines ported from {@code COACTUPC} and its shared date
 * copybook {@code CSUTLDPY.cpy}. Each method returns the exact {@code WS-RETURN-MSG} text
 * the COBOL edit would have produced for the first failing check, or {@code null} when the
 * value passes (COBOL only records the first message while {@code WS-RETURN-MSG} is still
 * {@code SPACES}, so at most one message per field is returned — matching the {@code GO TO
 * ...-EXIT} short-circuits in the original paragraphs).
 */
final class FieldEditors {

    private FieldEditors() {
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isAllDigits(String value) {
        String v = value.trim();
        if (v.isEmpty()) {
            return false;
        }
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAlphaOrSpace(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                return false;
            }
        }
        return true;
    }

    /** {@code 1220-EDIT-YESNO}: mandatory Y/N flag. */
    static String editYesNo(String name, String value) {
        if (isBlank(value) || value.trim().chars().allMatch(c -> c == '0')) {
            return name + " must be supplied.";
        }
        String v = value.trim();
        if (!v.equals("Y") && !v.equals("N")) {
            return name + " must be Y or N.";
        }
        return null;
    }

    /** {@code 1215-EDIT-MANDATORY}: value must be present. */
    static String editMandatory(String name, String value) {
        if (isBlank(value)) {
            return name + " must be supplied.";
        }
        return null;
    }

    /** {@code 1225-EDIT-ALPHA-REQD}: mandatory, alphabets and spaces only. */
    static String editAlphaReqd(String name, String value) {
        if (isBlank(value)) {
            return name + " must be supplied.";
        }
        if (!isAlphaOrSpace(value)) {
            return name + " can have alphabets only.";
        }
        return null;
    }

    /** {@code 1235-EDIT-ALPHA-OPT}: optional, but alphabets and spaces only when supplied. */
    static String editAlphaOpt(String name, String value) {
        if (isBlank(value)) {
            return null;
        }
        if (!isAlphaOrSpace(value)) {
            return name + " can have alphabets only.";
        }
        return null;
    }

    /** {@code 1245-EDIT-NUM-REQD}: mandatory, all numeric, non-zero. */
    static String editNumReqd(String name, String value) {
        if (isBlank(value)) {
            return name + " must be supplied.";
        }
        if (!isAllDigits(value)) {
            return name + " must be all numeric.";
        }
        if (new BigDecimal(value.trim()).signum() == 0) {
            return name + " must not be zero.";
        }
        return null;
    }

    /** {@code 1250-EDIT-SIGNED-9V2}: mandatory signed currency amount (S9(10)V99). */
    static String editSigned9v2(String name, String value) {
        if (isBlank(value)) {
            return name + " must be supplied.";
        }
        if (parseSigned9v2(value) == null) {
            return name + " is not valid";
        }
        return null;
    }

    /**
     * Parse a signed currency amount the way COBOL {@code TEST-NUMVAL-C}/{@code NUMVAL-C}
     * accept it: optional sign, digits, optional grouping commas and a decimal point.
     * Returns {@code null} when the value is not a valid amount.
     */
    static BigDecimal parseSigned9v2(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim().replace(",", "");
        if (v.isEmpty()) {
            return null;
        }
        if (!v.matches("[+-]?\\d*(\\.\\d+)?") || v.equals("+") || v.equals("-") || v.equals(".")) {
            return null;
        }
        try {
            return new BigDecimal(v).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
