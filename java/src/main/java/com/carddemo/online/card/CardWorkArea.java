package com.carddemo.online.card;

/**
 * COBOL copybook: CVCRD01Y — CC-WORK-AREAS, the common work area of the online card
 * programs COCRDLIC (CCLI), COCRDSLC (CCDL) and COCRDUPC (CCUP).
 *
 * <p>Holds the navigation literals (CCARD-NEXT-PROG / CCARD-NEXT-MAPSET /
 * CCARD-NEXT-MAP) and the normalization of the two search keys CC-ACCT-ID and
 * CC-CARD-NUM, which the programs treat as blank when they contain '*', spaces,
 * LOW-VALUES or zeroes.</p>
 */
public final class CardWorkArea {

    public static final int MAX_SCREEN_LINES = 7;

    public static final String LIST_PROGRAM = "COCRDLIC";
    public static final String LIST_TRANSACTION = "CCLI";
    public static final String LIST_MAPSET = "COCRDLI";
    public static final String LIST_MAP = "CCRDLIA";

    public static final String DETAIL_PROGRAM = "COCRDSLC";
    public static final String DETAIL_TRANSACTION = "CCDL";
    public static final String DETAIL_MAPSET = "COCRDSL";
    public static final String DETAIL_MAP = "CCRDSLA";

    public static final String UPDATE_PROGRAM = "COCRDUPC";
    public static final String UPDATE_TRANSACTION = "CCUP";
    public static final String UPDATE_MAPSET = "COCRDUP";
    public static final String UPDATE_MAP = "CCRDUPA";

    public static final String MENU_PROGRAM = "COMEN01C";
    public static final String MENU_TRANSACTION = "CM00";

    public static final String CARD_FILE = "CARDDAT";

    private CardWorkArea() {
    }

    /** '*', spaces and LOW-VALUES are treated as "not supplied". */
    public static String normalizeKey(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "*".equals(trimmed)) {
            return null;
        }
        return trimmed;
    }

    /** All-numeric test of a COBOL alphanumeric field (IS NOT NUMERIC). */
    public static boolean isNumeric(String value) {
        return value != null && !value.isEmpty() && value.chars().allMatch(Character::isDigit);
    }

    /** A numeric redefine equal to ZEROS also counts as "not supplied". */
    public static boolean isZeroes(String value) {
        return isNumeric(value) && value.chars().allMatch(c -> c == '0');
    }

    /**
     * WS-FILE-ERROR-MESSAGE: 'File Error: ' opname(8) ' on ' file(9)
     * ' returned RESP ' resp(10) ',RESP2 ' reas(10).
     */
    public static String fileErrorMessage(String operation, String file, int resp, int reason) {
        return "File Error: " + pad(operation, 8) + " on " + pad(file, 9) + " returned RESP "
                + pad(String.format("%09d", resp), 10) + ",RESP2 " + pad(String.format("%09d", reason), 10);
    }

    private static String pad(String value, int length) {
        String base = value == null ? "" : value;
        if (base.length() >= length) {
            return base.substring(0, length);
        }
        return base + " ".repeat(length - base.length());
    }
}
