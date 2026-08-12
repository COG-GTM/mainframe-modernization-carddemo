package com.carddemo.online.menu;

/**
 * COBOL paragraph: the option normalization at the head of PROCESS-ENTER-KEY in COMEN01C and
 * COADM01C.
 *
 * <pre>
 *   PERFORM VARYING WS-IDX FROM LENGTH OF OPTIONI BY -1 UNTIL
 *           OPTIONI(WS-IDX:1) NOT = SPACES OR WS-IDX = 1
 *   MOVE OPTIONI(1:WS-IDX) TO WS-OPTION-X   *&gt; PIC X(02) JUST RIGHT
 *   INSPECT WS-OPTION-X REPLACING ALL ' ' BY '0'
 *   MOVE WS-OPTION-X TO WS-OPTION           *&gt; PIC 9(02)
 * </pre>
 *
 * <p>So {@code "1 "} and {@code "1"} both become 1, a blank field becomes 0 and anything that is
 * not a pair of digits after the substitution is rejected as non numeric.
 */
final class MenuOptionParser {

    /** The parsed WS-OPTION, or {@code null} when {@code WS-OPTION IS NOT NUMERIC}. */
    static Integer parse(String input) {
        String field = input == null ? "" : input;
        field = field.length() >= 2 ? field.substring(0, 2) : String.format("%-2s", field);

        int index = 2;
        while (index > 1 && field.charAt(index - 1) == ' ') {
            index--;
        }
        String justifiedRight = String.format("%2s", field.substring(0, index));
        String digits = justifiedRight.replace(' ', '0');

        if (!Character.isDigit(digits.charAt(0)) || !Character.isDigit(digits.charAt(1))) {
            return null;
        }
        return Integer.parseInt(digits);
    }

    private MenuOptionParser() {
    }
}
