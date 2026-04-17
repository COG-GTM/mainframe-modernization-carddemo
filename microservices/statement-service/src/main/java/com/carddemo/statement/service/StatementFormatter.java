package com.carddemo.statement.service;

import com.carddemo.statement.dto.AccountData;
import com.carddemo.statement.dto.TransactionData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Formats statement output in plain text and HTML formats.
 * Directly translates CBSTM03A's STATEMENT-LINES and HTML-LINES structures.
 *
 * <p>The COBOL program generated two output files simultaneously:
 * STMT-FILE (80-char fixed-width line printer format) and
 * HTML-FILE (100-char HTML with inline styles).</p>
 */
@Component
public class StatementFormatter {

    private static final String SEPARATOR_LINE = "-".repeat(80);
    private static final String STAR_LINE = "*".repeat(80);

    /**
     * Generate plain text statement output.
     * Mirrors CBSTM03A's STATEMENT-LINES structure (ST-LINE0 through ST-LINE15).
     */
    public String formatTextStatement(AccountData account, List<TransactionData> transactions,
                                      String startDate, String endDate) {
        StringBuilder sb = new StringBuilder();

        // ST-LINE0: Start of statement marker
        sb.append("*".repeat(31)).append("START OF STATEMENT").append("*".repeat(31)).append("\n");

        // ST-LINE1: Customer name
        String customerName = account.getCustomerName() != null ? account.getCustomerName() : "";
        sb.append(padRight(customerName, 75)).append("\n");

        // ST-LINE2-4: Address lines
        sb.append(padRight(nullSafe(account.getAddressLine1()), 50)).append("\n");
        sb.append(padRight(nullSafe(account.getAddressLine2()), 50)).append("\n");
        String fullAddress = buildFullAddress(account);
        sb.append(padRight(fullAddress, 80)).append("\n");

        // ST-LINE5: Separator
        sb.append(SEPARATOR_LINE).append("\n");

        // ST-LINE6: Basic Details header
        sb.append(padCenter("Basic Details", 80)).append("\n");

        // ST-LINE5: Separator
        sb.append(SEPARATOR_LINE).append("\n");

        // ST-LINE7: Account ID
        sb.append("Account ID         :").append(padRight(nullSafe(account.getAccountId()), 20)).append("\n");

        // ST-LINE8: Current Balance (mirrors PIC 9(9).99-)
        sb.append("Current Balance    :").append(formatAmount(account.getCurrentBalance())).append("\n");

        // ST-LINE9: FICO Score
        sb.append("FICO Score         :").append(padRight(String.valueOf(account.getFicoScore()), 20)).append("\n");

        // Credit Limit and Available Credit
        sb.append("Credit Limit       :").append(formatAmount(account.getCreditLimit())).append("\n");
        BigDecimal availableCredit = account.getCreditLimit() != null && account.getCurrentBalance() != null
                ? account.getCreditLimit().subtract(account.getCurrentBalance())
                : BigDecimal.ZERO;
        sb.append("Available Credit   :").append(formatAmount(availableCredit)).append("\n");

        // Statement period
        sb.append("Statement Period   :").append(startDate).append(" to ").append(endDate).append("\n");

        // ST-LINE10: Separator
        sb.append(SEPARATOR_LINE).append("\n");

        // ST-LINE11: Transaction summary header
        sb.append(padCenter("TRANSACTION SUMMARY", 80)).append("\n");

        // ST-LINE12: Separator
        sb.append(SEPARATOR_LINE).append("\n");

        // ST-LINE13: Column headers (mirrors COBOL format)
        sb.append(padRight("Tran ID", 16))
          .append(" ")
          .append(padRight("Tran Details", 49))
          .append("  Tran Amount").append("\n");

        // ST-LINE12: Separator
        sb.append(SEPARATOR_LINE).append("\n");

        // ST-LINE14: Transaction lines
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (TransactionData txn : transactions) {
            String tranId = padRight(nullSafe(txn.getTransactionId()), 16);
            String tranDesc = padRight(nullSafe(txn.getDescription()), 49);
            BigDecimal amt = txn.getAmount() != null ? txn.getAmount() : BigDecimal.ZERO;
            sb.append(tranId).append(" ").append(tranDesc).append("$").append(formatAmountValue(amt)).append("\n");

            if (amt.compareTo(BigDecimal.ZERO) >= 0) {
                totalDebits = totalDebits.add(amt);
            } else {
                totalCredits = totalCredits.add(amt.abs());
            }
        }

        // Totals section
        sb.append(SEPARATOR_LINE).append("\n");
        sb.append("Total Debits  : $").append(formatAmountValue(totalDebits)).append("\n");
        sb.append("Total Credits : $").append(formatAmountValue(totalCredits)).append("\n");

        // ST-LINE14A: Total EXP (mirrors COBOL)
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TransactionData txn : transactions) {
            if (txn.getAmount() != null) {
                totalAmount = totalAmount.add(txn.getAmount());
            }
        }
        sb.append("Total EXP:").append(" ".repeat(56)).append("$").append(formatAmountValue(totalAmount)).append("\n");

        // ST-LINE15: End of statement marker
        sb.append("*".repeat(32)).append("END OF STATEMENT").append("*".repeat(32)).append("\n");

        return sb.toString();
    }

    /**
     * Generate HTML statement output.
     * Mirrors CBSTM03A's HTML-LINES structure with inline CSS styles.
     */
    public String formatHtmlStatement(AccountData account, List<TransactionData> transactions,
                                      String startDate, String endDate) {
        StringBuilder sb = new StringBuilder();

        // HTML header (mirrors HTML-L01 through HTML-L08)
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("<meta charset=\"utf-8\">\n");
        sb.append("<title>Account Statement</title>\n");
        sb.append("</head>\n");
        sb.append("<body style=\"margin:0px;\">\n");
        sb.append("<table align=\"center\" frame=\"box\" style=\"width:70%; font:12px Segoe UI,sans-serif;\">\n");

        // Header row with account number (mirrors HTML-L10, HTML-L11)
        sb.append("<tr>\n");
        sb.append("<td colspan=\"3\" style=\"padding:0px 5px; background-color:#1d1d96b3;\">\n");
        sb.append("<h3>Statement for Account Number: ").append(nullSafe(account.getAccountId())).append("</h3>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // Bank info row (mirrors HTML-L15 through HTML-L18)
        sb.append("<tr>\n");
        sb.append("<td colspan=\"3\" style=\"padding:0px 5px; background-color:#FFAF33;\">\n");
        sb.append("<p style=\"font-size:16px\">Bank of XYZ</p>\n");
        sb.append("<p>410 Terry Ave N</p>\n");
        sb.append("<p>Seattle WA 99999</p>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // Customer name and address (mirrors HTML-L22-35, HTML-L23)
        sb.append("<tr>\n");
        sb.append("<td colspan=\"3\" style=\"padding:0px 5px; background-color:#f2f2f2;\">\n");
        String customerName = nullSafe(account.getCustomerName()).trim();
        sb.append("<p style=\"font-size:16px\">").append(customerName).append("</p>\n");
        sb.append("<p>").append(nullSafe(account.getAddressLine1()).trim()).append("</p>\n");
        sb.append("<p>").append(nullSafe(account.getAddressLine2()).trim()).append("</p>\n");
        sb.append("<p>").append(buildFullAddress(account).trim()).append("</p>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // Basic Details section (mirrors HTML-L30-42, HTML-L31)
        sb.append("<tr>\n");
        sb.append("<td colspan=\"3\" style=\"padding:0px 5px; background-color:#33FFD1; text-align:center;\">\n");
        sb.append("<p style=\"font-size:16px\">Basic Details</p>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // Account details
        sb.append("<tr>\n");
        sb.append("<td colspan=\"3\" style=\"padding:0px 5px; background-color:#f2f2f2;\">\n");
        sb.append("<p>Account ID         : ").append(nullSafe(account.getAccountId())).append("</p>\n");
        sb.append("<p>Current Balance    : ").append(formatAmount(account.getCurrentBalance())).append("</p>\n");
        sb.append("<p>FICO Score         : ").append(account.getFicoScore()).append("</p>\n");
        sb.append("<p>Credit Limit       : ").append(formatAmount(account.getCreditLimit())).append("</p>\n");

        BigDecimal availableCredit = account.getCreditLimit() != null && account.getCurrentBalance() != null
                ? account.getCreditLimit().subtract(account.getCurrentBalance())
                : BigDecimal.ZERO;
        sb.append("<p>Available Credit   : ").append(formatAmount(availableCredit)).append("</p>\n");
        sb.append("<p>Statement Period   : ").append(startDate).append(" to ").append(endDate).append("</p>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // Transaction Summary header (mirrors HTML-L43)
        sb.append("<tr>\n");
        sb.append("<td colspan=\"3\" style=\"padding:0px 5px; background-color:#33FFD1; text-align:center;\">\n");
        sb.append("<p style=\"font-size:16px\">Transaction Summary</p>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // Transaction column headers (mirrors HTML-L47 through HTML-L54)
        sb.append("<tr>\n");
        sb.append("<td style=\"width:25%; padding:0px 5px; background-color:#33FF5E; text-align:left;\">\n");
        sb.append("<p style=\"font-size:16px\">Tran ID</p>\n");
        sb.append("</td>\n");
        sb.append("<td style=\"width:55%; padding:0px 5px; background-color:#33FF5E; text-align:left;\">\n");
        sb.append("<p style=\"font-size:16px\">Tran Details</p>\n");
        sb.append("</td>\n");
        sb.append("<td style=\"width:20%; padding:0px 5px; background-color:#33FF5E; text-align:right;\">\n");
        sb.append("<p style=\"font-size:16px\">Amount</p>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // Transaction rows (mirrors HTML-L58, HTML-L61, HTML-L64 pattern)
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (TransactionData txn : transactions) {
            BigDecimal amt = txn.getAmount() != null ? txn.getAmount() : BigDecimal.ZERO;

            sb.append("<tr>\n");
            sb.append("<td style=\"width:25%; padding:0px 5px; background-color:#f2f2f2; text-align:left;\">\n");
            sb.append("<p>").append(nullSafe(txn.getTransactionId())).append("</p>\n");
            sb.append("</td>\n");
            sb.append("<td style=\"width:55%; padding:0px 5px; background-color:#f2f2f2; text-align:left;\">\n");
            sb.append("<p>").append(nullSafe(txn.getDescription())).append("</p>\n");
            sb.append("</td>\n");
            sb.append("<td style=\"width:20%; padding:0px 5px; background-color:#f2f2f2; text-align:right;\">\n");
            sb.append("<p>$").append(formatAmountValue(amt)).append("</p>\n");
            sb.append("</td>\n");
            sb.append("</tr>\n");

            if (amt.compareTo(BigDecimal.ZERO) >= 0) {
                totalDebits = totalDebits.add(amt);
            } else {
                totalCredits = totalCredits.add(amt.abs());
            }
        }

        // Totals row
        sb.append("<tr>\n");
        sb.append("<td colspan=\"2\" style=\"padding:0px 5px; background-color:#f2f2f2; text-align:left;\">\n");
        sb.append("<p>Total Debits: $").append(formatAmountValue(totalDebits)).append("</p>\n");
        sb.append("<p>Total Credits: $").append(formatAmountValue(totalCredits)).append("</p>\n");
        sb.append("</td>\n");
        sb.append("<td style=\"padding:0px 5px; background-color:#f2f2f2; text-align:right;\">\n");
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TransactionData txn : transactions) {
            if (txn.getAmount() != null) {
                totalAmount = totalAmount.add(txn.getAmount());
            }
        }
        sb.append("<p><strong>Total: $").append(formatAmountValue(totalAmount)).append("</strong></p>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");

        // End of statement (mirrors HTML-L75 through HTML-L80)
        sb.append("<tr>\n");
        sb.append("<td colspan=\"3\" style=\"padding:0px 5px; background-color:#1d1d96b3;\">\n");
        sb.append("<h3>End of Statement</h3>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }

    private String buildFullAddress(AccountData account) {
        StringBuilder addr = new StringBuilder();
        String line3 = nullSafe(account.getAddressLine3()).trim();
        if (!line3.isEmpty()) {
            addr.append(line3).append(" ");
        }
        String state = nullSafe(account.getStateCode()).trim();
        if (!state.isEmpty()) {
            addr.append(state).append(" ");
        }
        String country = nullSafe(account.getCountryCode()).trim();
        if (!country.isEmpty()) {
            addr.append(country).append(" ");
        }
        String zip = nullSafe(account.getZipCode()).trim();
        if (!zip.isEmpty()) {
            addr.append(zip);
        }
        return addr.toString().trim();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%,.2f", amount);
    }

    private String formatAmountValue(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%,.2f", amount);
    }

    private String padRight(String str, int length) {
        if (str == null) {
            return " ".repeat(length);
        }
        if (str.length() >= length) {
            return str.substring(0, length);
        }
        return str + " ".repeat(length - str.length());
    }

    private String padCenter(String str, int width) {
        int padding = (width - str.length()) / 2;
        return " ".repeat(padding) + str + " ".repeat(width - str.length() - padding);
    }

    private String nullSafe(String str) {
        return str != null ? str : "";
    }
}
