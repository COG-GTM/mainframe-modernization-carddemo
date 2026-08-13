package com.carddemo.online.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * COBOL program: COACTUPC — field edit routines (1210-EDIT-ACCOUNT, 1215-EDIT-MANDATORY,
 * 1220-EDIT-YESNO, 1225-EDIT-ALPHA-REQD, 1235-EDIT-ALPHA-OPT, 1245-EDIT-NUM-REQD,
 * 1250-EDIT-SIGNED-9V2, 1260-EDIT-US-PHONE-NUM, 1265-EDIT-US-SSN, 1270-EDIT-US-STATE-CD,
 * 1275-EDIT-FICO-SCORE, 1280-EDIT-US-STATE-ZIP-CD).
 *
 * <p>Copybooks: CSUTLDPY / CSUTLDWY (date edits EDIT-DATE-CCYYMMDD and EDIT-DATE-OF-BIRTH),
 * CSLKPCDY (lookup tables, see {@link AccountLookupCodes}).</p>
 *
 * <p>Every message text is reproduced character for character from the COBOL source.</p>
 */
public final class AccountFieldValidator {

    private static final Pattern ALPHA_SPACES = Pattern.compile("[A-Za-z ]*");
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern SIGNED_9V2 = Pattern.compile("[+-]?\\d{1,10}(\\.\\d{1,2})?[+-]?");

    private AccountFieldValidator() {
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    /** 1215-EDIT-MANDATORY. */
    public static boolean editMandatory(AccountValidationResult result, String field, String name, String value) {
        if (isBlank(value)) {
            result.addError(field, name + " must be supplied.");
            return false;
        }
        return true;
    }

    /** 1220-EDIT-YESNO. */
    public static boolean editYesNo(AccountValidationResult result, String field, String name, String value) {
        if (isBlank(value) || "0".equals(trimmed(value))) {
            result.addError(field, name + " must be supplied.");
            return false;
        }
        String candidate = trimmed(value);
        if (!"Y".equals(candidate) && !"N".equals(candidate)) {
            result.addError(field, name + " must be Y or N.");
            return false;
        }
        return true;
    }

    /** 1225-EDIT-ALPHA-REQD. */
    public static boolean editAlphaRequired(AccountValidationResult result, String field, String name, String value) {
        if (isBlank(value)) {
            result.addError(field, name + " must be supplied.");
            return false;
        }
        if (!ALPHA_SPACES.matcher(value).matches()) {
            result.addError(field, name + " can have alphabets only.");
            return false;
        }
        return true;
    }

    /** 1235-EDIT-ALPHA-OPT. */
    public static boolean editAlphaOptional(AccountValidationResult result, String field, String name, String value) {
        if (isBlank(value)) {
            return true;
        }
        if (!ALPHA_SPACES.matcher(value).matches()) {
            result.addError(field, name + " can have alphabets only.");
            return false;
        }
        return true;
    }

    /** 1245-EDIT-NUM-REQD. */
    public static boolean editNumericRequired(AccountValidationResult result, String field, String name,
            String value) {
        if (isBlank(value)) {
            result.addError(field, name + " must be supplied.");
            return false;
        }
        String candidate = trimmed(value);
        if (!DIGITS.matcher(candidate).matches()) {
            result.addError(field, name + " must be all numeric.");
            return false;
        }
        if (new BigDecimal(candidate).signum() == 0) {
            result.addError(field, name + " must not be zero.");
            return false;
        }
        return true;
    }

    /** 1250-EDIT-SIGNED-9V2 (FUNCTION TEST-NUMVAL-C). */
    public static boolean editSigned9v2(AccountValidationResult result, String field, String name, String value) {
        if (value == null || value.trim().isEmpty()) {
            result.addError(field, name + " must be supplied.");
            return false;
        }
        if (parseSigned9v2(value) == null) {
            result.addError(field, name + " is not valid");
            return false;
        }
        return true;
    }

    /**
     * Numeric value of an edited S9(10)V99 field, or {@code null} when the COBOL
     * {@code FUNCTION TEST-NUMVAL-C} check would fail.
     */
    public static BigDecimal parseSigned9v2(String value) {
        if (value == null) {
            return null;
        }
        String candidate = value.trim().replace(",", "").replace("$", "");
        if (candidate.isEmpty() || !SIGNED_9V2.matcher(candidate).matches()) {
            return null;
        }
        boolean negative = candidate.startsWith("-") || candidate.endsWith("-");
        String digits = candidate.replace("+", "").replace("-", "");
        BigDecimal parsed = new BigDecimal(digits);
        return negative ? parsed.negate() : parsed;
    }

    /** 1260-EDIT-US-PHONE-NUM, paragraphs EDIT-AREA-CODE / EDIT-US-PHONE-PREFIX / EDIT-US-PHONE-LINENUM. */
    public static void editUsPhoneNumber(AccountValidationResult result, String field, String name,
            String areaCode, String prefix, String lineNumber) {
        if (isBlank(areaCode) && isBlank(prefix) && isBlank(lineNumber)) {
            return;
        }

        if (isBlank(areaCode)) {
            result.addError(field, name + ": Area code must be supplied.");
        } else if (!DIGITS.matcher(trimmed(areaCode)).matches()) {
            result.addError(field, name + ": Area code must be A 3 digit number.");
        } else if (new BigDecimal(trimmed(areaCode)).signum() == 0) {
            result.addError(field, name + ": Area code cannot be zero");
        } else if (!AccountLookupCodes.isValidGeneralPurposeAreaCode(areaCode)) {
            result.addError(field, name + ": Not valid North America general purpose area code");
        }

        if (isBlank(prefix)) {
            result.addError(field, name + ": Prefix code must be supplied.");
        } else if (!DIGITS.matcher(trimmed(prefix)).matches()) {
            result.addError(field, name + ": Prefix code must be A 3 digit number.");
        } else if (new BigDecimal(trimmed(prefix)).signum() == 0) {
            result.addError(field, name + ": Prefix code cannot be zero");
        }

        if (isBlank(lineNumber)) {
            result.addError(field, name + ": Line number code must be supplied.");
        } else if (!DIGITS.matcher(trimmed(lineNumber)).matches()) {
            result.addError(field, name + ": Line number code must be A 4 digit number.");
        } else if (new BigDecimal(trimmed(lineNumber)).signum() == 0) {
            result.addError(field, name + ": Line number code cannot be zero");
        }
    }

    /** 1265-EDIT-US-SSN. */
    public static void editUsSsn(AccountValidationResult result, String field, String part1, String part2,
            String part3) {
        boolean part1Valid = editNumericRequired(result, field, "SSN: First 3 chars", part1);
        if (part1Valid) {
            int value = Integer.parseInt(trimmed(part1));
            if (value == 0 || value == 666 || (value >= 900 && value <= 999)) {
                result.addError(field, "SSN: First 3 chars: should not be 000, 666, or between 900 and 999");
            }
        }
        editNumericRequired(result, field, "SSN 4th & 5th chars", part2);
        editNumericRequired(result, field, "SSN Last 4 chars", part3);
    }

    /** 1270-EDIT-US-STATE-CD. */
    public static void editUsStateCode(AccountValidationResult result, String field, String name, String stateCode) {
        if (!AccountLookupCodes.isValidUsStateCode(stateCode)) {
            result.addError(field, name + ": is not a valid state code");
        }
    }

    /** 1275-EDIT-FICO-SCORE. */
    public static void editFicoScore(AccountValidationResult result, String field, String name, String ficoScore) {
        int score = Integer.parseInt(trimmed(ficoScore));
        if (score < 300 || score > 850) {
            result.addError(field, name + ": should be between 300 and 850");
        }
    }

    /** 1280-EDIT-US-STATE-ZIP-CD — flags both the state and the zip field. */
    public static void editStateZipCombination(AccountValidationResult result, String stateField, String zipField,
            String stateCode, String zip) {
        if (!AccountLookupCodes.isValidStateZipCombo(stateCode, zip)) {
            result.addError(stateField, "Invalid zip code for state");
            result.addError(zipField, "Invalid zip code for state");
        }
    }

    /**
     * CSUTLDPY EDIT-DATE-CCYYMMDD (paragraphs EDIT-YEAR-CCYY, EDIT-MONTH, EDIT-DAY,
     * EDIT-DAY-MONTH-YEAR and the LE date validation call).
     *
     * @return true when the date passed every edit
     */
    public static boolean editDateCcyymmdd(AccountValidationResult result, String field, String name,
            String year, String month, String day) {
        boolean yearOk = true;
        boolean monthOk = true;
        boolean dayOk = true;

        if (isBlank(year)) {
            result.addError(field, name + " : Year must be supplied.");
            yearOk = false;
        } else if (!DIGITS.matcher(trimmed(year)).matches() || trimmed(year).length() != 4) {
            result.addError(field, name + " must be 4 digit number.");
            yearOk = false;
        } else {
            int century = Integer.parseInt(trimmed(year).substring(0, 2));
            if (century != 19 && century != 20) {
                result.addError(field, name + " : Century is not valid.");
                yearOk = false;
            }
        }

        if (isBlank(month)) {
            result.addError(field, name + " : Month must be supplied.");
            monthOk = false;
        } else if (!DIGITS.matcher(trimmed(month)).matches()
                || Integer.parseInt(trimmed(month)) < 1
                || Integer.parseInt(trimmed(month)) > 12) {
            result.addError(field, name + ": Month must be a number between 1 and 12.");
            monthOk = false;
        }

        if (isBlank(day)) {
            result.addError(field, name + " : Day must be supplied.");
            dayOk = false;
        } else if (!DIGITS.matcher(trimmed(day)).matches()
                || Integer.parseInt(trimmed(day)) < 1
                || Integer.parseInt(trimmed(day)) > 31) {
            result.addError(field, name + ":day must be a number between 1 and 31.");
            dayOk = false;
        }

        if (!monthOk || !dayOk) {
            return false;
        }

        int monthValue = Integer.parseInt(trimmed(month));
        int dayValue = Integer.parseInt(trimmed(day));
        boolean monthWith31Days = monthValue == 1 || monthValue == 3 || monthValue == 5 || monthValue == 7
                || monthValue == 8 || monthValue == 10 || monthValue == 12;

        if (!monthWith31Days && dayValue == 31) {
            result.addError(field, name + ":Cannot have 31 days in this month.");
            return false;
        }
        if (monthValue == 2 && dayValue == 30) {
            result.addError(field, name + ":Cannot have 30 days in this month.");
            return false;
        }
        if (monthValue == 2 && dayValue == 29) {
            if (!yearOk) {
                return false;
            }
            int yearValue = Integer.parseInt(trimmed(year));
            int divisor = yearValue % 100 == 0 ? 400 : 4;
            if (yearValue % divisor != 0) {
                result.addError(field, name + ":Not a leap year.Cannot have 29 days in this month.");
                return false;
            }
        }

        if (!yearOk) {
            return false;
        }

        try {
            LocalDate.of(Integer.parseInt(trimmed(year)), monthValue, dayValue);
        } catch (java.time.DateTimeException e) {
            result.addError(field, name + " validation error Sev code: ");
            return false;
        }
        return true;
    }

    /** CSUTLDPY EDIT-DATE-OF-BIRTH — a date of birth cannot be today or in the future. */
    public static boolean editDateOfBirth(AccountValidationResult result, String field, String name,
            String year, String month, String day, LocalDate today) {
        LocalDate dateOfBirth = LocalDate.of(Integer.parseInt(trimmed(year)), Integer.parseInt(trimmed(month)),
                Integer.parseInt(trimmed(day)));
        if (today.isAfter(dateOfBirth)) {
            return true;
        }
        result.addError(field, name + ":cannot be in the future ");
        return false;
    }

    /** 1210-EDIT-ACCOUNT — the 11 digit non zero account filter. */
    public static boolean editAccountFilter(AccountValidationResult result, String field, String accountId) {
        if (isBlank(accountId)) {
            result.addError(field, "Account number not provided");
            return false;
        }
        String candidate = trimmed(accountId);
        if (!DIGITS.matcher(candidate).matches() || candidate.length() != 11
                || new BigDecimal(candidate).signum() == 0) {
            result.addError(field, "Account Number if supplied must be a 11 digit Non-Zero Number");
            return false;
        }
        return true;
    }
}
