package com.carddemo.batch.statement;

import com.carddemo.batch.statement.AccountStatement.StatementTransaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an {@link AccountStatement} into the two output formats produced by legacy
 * {@code CBSTM03A}: a fixed-width 80-column plain-text statement (the {@code ST-LINEnn} layout
 * defined in the program's WORKING-STORAGE, driven by {@code COSTM01}) and the equivalent HTML
 * table.
 *
 * <p>The plain-text output is byte-for-byte faithful to the COBOL edited pictures — including
 * {@code PIC 9(9).99-} for the account balance (leading zeros kept, trailing sign) and
 * {@code PIC Z(9).99-} for transaction amounts (leading zeros suppressed to spaces, trailing
 * sign). Every text line is exactly {@value #TEXT_WIDTH} characters. The HTML output reproduces
 * the same table structure/labels as the COBOL {@code HTML-*} lines but is emitted as clean
 * markup (no 100-byte record padding).</p>
 */
public final class StatementFormatter {

    /** COBOL STMT-FILE record width — {@code FD-STMTFILE-REC PIC X(80)}. */
    public static final int TEXT_WIDTH = 80;

    private StatementFormatter() {
    }

    // --- plain text (STMTFILE) --------------------------------------------------------------

    /** Render the fixed-width plain-text statement as a list of 80-column lines. */
    public static List<String> toTextLines(AccountStatement s) {
        List<String> out = new ArrayList<>();
        // ST-LINE0 : 31 '*' + "START OF STATEMENT" + 31 '*'
        out.add(rep('*', 31) + "START OF STATEMENT" + rep('*', 31));
        // ST-LINE1 : ST-NAME PIC X(75) + 5 spaces
        out.add(fit(s.cardholderName(), 75) + rep(' ', 5));
        // ST-LINE2 : ST-ADD1 PIC X(50) + 30 spaces
        out.add(fit(s.addressLine1(), 50) + rep(' ', 30));
        // ST-LINE3 : ST-ADD2 PIC X(50) + 30 spaces
        out.add(fit(s.addressLine2(), 50) + rep(' ', 30));
        // ST-LINE4 : ST-ADD3 PIC X(80)
        out.add(fit(s.addressLine3(), 80));
        // ST-LINE5 : 80 '-'
        String dashes = rep('-', 80);
        out.add(dashes);
        // ST-LINE6 : 33 spaces + "Basic Details" (X14) + 33 spaces
        out.add(rep(' ', 33) + fit("Basic Details", 14) + rep(' ', 33));
        out.add(dashes);
        // ST-LINE7 : "Account ID         :" + ST-ACCT-ID PIC X(20) + 40 spaces
        out.add("Account ID         :" + fit(s.acctId(), 20) + rep(' ', 40));
        // ST-LINE8 : "Current Balance    :" + ST-CURR-BAL PIC 9(9).99- + 7 spaces + 40 spaces
        out.add("Current Balance    :" + editZoned(s.currentBalance(), 9, false)
                + rep(' ', 7) + rep(' ', 40));
        // ST-LINE9 : "FICO Score         :" + ST-FICO-SCORE PIC X(20) + 40 spaces
        out.add("FICO Score         :" + fit(fico(s.ficoScore()), 20) + rep(' ', 40));
        out.add(dashes);
        // ST-LINE11 : 30 spaces + "TRANSACTION SUMMARY " (X20) + 30 spaces
        out.add(rep(' ', 30) + "TRANSACTION SUMMARY " + rep(' ', 30));
        out.add(dashes);
        // ST-LINE13 : "Tran ID         " + "Tran Details    "(X51) + "  Tran Amount"
        out.add("Tran ID         " + fit("Tran Details    ", 51) + "  Tran Amount");
        out.add(dashes);
        // ST-LINE14 per transaction
        for (StatementTransaction t : s.transactions()) {
            // ST-TRANID X(16) + ' ' + ST-TRANDT X(49) + '$' + ST-TRANAMT Z(9).99-
            out.add(fit(t.tranId(), 16) + " " + fit(t.description(), 49) + "$"
                    + editZoned(t.amount(), 9, true));
        }
        out.add(dashes);
        // ST-LINE14A : "Total EXP:" + 56 spaces + '$' + ST-TOTAL-TRAMT Z(9).99-
        out.add("Total EXP:" + rep(' ', 56) + "$" + editZoned(s.totalAmount(), 9, true));
        // ST-LINE15 : 32 '*' + "END OF STATEMENT" + 32 '*'
        out.add(rep('*', 32) + "END OF STATEMENT" + rep('*', 32));
        return out;
    }

    // --- HTML (HTMLFILE) --------------------------------------------------------------------

    private static final String TD_HEADER = "background-color:#1d1d96b3;";
    private static final String TD_BANK = "background-color:#FFAF33;";
    private static final String TD_LIGHT = "background-color:#f2f2f2;";
    private static final String TD_SECTION = "background-color:#33FFD1; text-align:center;";

    /** Render the HTML statement as a list of markup lines (COBOL {@code HTML-*} lines). */
    public static List<String> toHtmlLines(AccountStatement s) {
        List<String> h = new ArrayList<>();
        h.add("<!DOCTYPE html>");
        h.add("<html lang=\"en\">");
        h.add("<head>");
        h.add("<meta charset=\"utf-8\">");
        h.add("<title>HTML Table Layout</title>");
        h.add("</head>");
        h.add("<body style=\"margin:0px;\">");
        h.add("<table  align=\"center\" frame=\"box\" style=\"width:70%; font:12px Segoe UI,sans-serif;\">");
        // account header band
        h.add("<tr>");
        h.add(tdColspan3(TD_HEADER));
        h.add("<h3>Statement for Account Number: " + trim(s.acctId()) + "</h3>");
        h.add("</td>");
        h.add("</tr>");
        // bank address band
        h.add("<tr>");
        h.add(tdColspan3(TD_BANK));
        h.add("<p style=\"font-size:16px\">Bank of XYZ</p>");
        h.add("<p>410 Terry Ave N</p>");
        h.add("<p>Seattle WA 99999</p>");
        h.add("</td>");
        h.add("</tr>");
        // cardholder name + address band
        h.add("<tr>");
        h.add(tdColspan3(TD_LIGHT));
        h.add("<p style=\"font-size:16px\">" + trim(s.cardholderName()) + "</p>");
        h.add("<p>" + trim(s.addressLine1()) + "</p>");
        h.add("<p>" + trim(s.addressLine2()) + "</p>");
        h.add("<p>" + trim(s.addressLine3()) + "</p>");
        h.add("</td>");
        h.add("</tr>");
        // Basic Details section header
        h.add("<tr>");
        h.add(tdColspan3(TD_SECTION));
        h.add("<p style=\"font-size:16px\">Basic Details</p>");
        h.add("</td>");
        h.add("</tr>");
        // Basic Details body
        h.add("<tr>");
        h.add(tdColspan3(TD_LIGHT));
        h.add("<p>Account ID         : " + fit(s.acctId(), 20) + "</p>");
        h.add("<p>Current Balance    : " + editZoned(s.currentBalance(), 9, false) + "</p>");
        h.add("<p>FICO Score         : " + fit(fico(s.ficoScore()), 20) + "</p>");
        h.add("</td>");
        h.add("</tr>");
        // Transaction Summary section header
        h.add("<tr>");
        h.add(tdColspan3(TD_SECTION));
        h.add("<p style=\"font-size:16px\">Transaction Summary</p>");
        h.add("</td>");
        h.add("</tr>");
        // column headers
        h.add("<tr>");
        h.add(td("width:25%", "#33FF5E", "left"));
        h.add("<p style=\"font-size:16px\">Tran ID</p>");
        h.add("</td>");
        h.add(td("width:55%", "#33FF5E", "left"));
        h.add("<p style=\"font-size:16px\">Tran Details</p>");
        h.add("</td>");
        h.add(td("width:20%", "#33FF5E", "right"));
        h.add("<p style=\"font-size:16px\">Amount</p>");
        h.add("</td>");
        h.add("</tr>");
        // transaction rows
        for (StatementTransaction t : s.transactions()) {
            h.add("<tr>");
            h.add(td("width:25%", "#f2f2f2", "left"));
            h.add("<p>" + trim(t.tranId()) + "</p>");
            h.add("</td>");
            h.add(td("width:55%", "#f2f2f2", "left"));
            h.add("<p>" + trim(fit(t.description(), 49)) + "</p>");
            h.add("</td>");
            h.add(td("width:20%", "#f2f2f2", "right"));
            h.add("<p>" + editZoned(t.amount(), 9, true) + "</p>");
            h.add("</td>");
            h.add("</tr>");
        }
        // end of statement band
        h.add("<tr>");
        h.add(tdColspan3(TD_HEADER));
        h.add("<h3>End of Statement</h3>");
        h.add("</td>");
        h.add("</tr>");
        h.add("</table>");
        h.add("</body>");
        h.add("</html>");
        return h;
    }

    private static String tdColspan3(String style) {
        return "<td colspan=\"3\" style=\"padding:0px 5px;" + style + "\">";
    }

    private static String td(String width, String color, String align) {
        return "<td style=\"" + width + "; padding:0px 5px; background-color:" + color
                + "; text-align:" + align + ";\">";
    }

    // --- COBOL edited-picture helpers -------------------------------------------------------

    /**
     * Format a {@link BigDecimal} as a COBOL trailing-sign edited number with two decimals.
     *
     * @param value       the value (null treated as zero)
     * @param intDigits   integer digit positions ({@code 9} for both {@code 9(9)} and {@code Z(9)})
     * @param suppress    {@code true} for {@code PIC Z(9).99-} (leading zeros → spaces),
     *                    {@code false} for {@code PIC 9(9).99-} (leading zeros kept)
     * @return the edited string, width {@code intDigits + 4} (digits + '.' + 2 decimals + sign)
     */
    static String editZoned(BigDecimal value, int intDigits, boolean suppress) {
        BigDecimal v = value == null ? BigDecimal.ZERO : value;
        boolean negative = v.signum() < 0;
        BigDecimal abs = v.abs().setScale(2, RoundingMode.HALF_UP);
        String plain = abs.toPlainString();
        int dot = plain.indexOf('.');
        String intPart = plain.substring(0, dot);
        String frac = plain.substring(dot + 1);
        if (intPart.length() > intDigits) {
            // COBOL truncates high-order digits on overflow of the receiving field.
            intPart = intPart.substring(intPart.length() - intDigits);
        }
        StringBuilder digits = new StringBuilder();
        for (int i = intPart.length(); i < intDigits; i++) {
            digits.append('0');
        }
        digits.append(intPart);
        if (suppress) {
            for (int i = 0; i < digits.length() - 1; i++) {
                if (digits.charAt(i) == '0') {
                    digits.setCharAt(i, ' ');
                } else {
                    break;
                }
            }
            // A whole-zero integer part suppresses its final digit too.
            if (digits.charAt(digits.length() - 1) == '0'
                    && (digits.length() == 1 || digits.charAt(digits.length() - 2) == ' ')) {
                digits.setCharAt(digits.length() - 1, ' ');
            }
        }
        return digits + "." + frac + (negative ? "-" : " ");
    }

    /** CUST-FICO-CREDIT-SCORE PIC 9(03) moved into a display field (left-justified digits). */
    private static String fico(Integer score) {
        if (score == null) {
            return "";
        }
        return String.format("%03d", score);
    }

    /** Left-justify into a fixed-width field, truncating or space-padding (COBOL MOVE to X(n)). */
    static String fit(String value, int width) {
        String v = value == null ? "" : value;
        if (v.length() >= width) {
            return v.substring(0, width);
        }
        return v + rep(' ', width - v.length());
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private static String rep(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }
}
