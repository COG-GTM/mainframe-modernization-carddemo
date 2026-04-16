package com.cardemo.batch.formatter;

import com.cardemo.batch.entity.Account;
import com.cardemo.batch.entity.Customer;
import com.cardemo.batch.entity.TransactionRecord;
import com.cardemo.batch.service.StatementData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates HTML statements matching the COBOL HTML-LINES structure
 * from CBSTM03A (sections 5100-WRITE-HTML-HEADER and 5200-WRITE-HTML-NMADBS).
 * Styled with inline CSS matching the original COBOL 88-level values.
 */
@Component
public class HtmlStatementFormatter {

    public List<String> format(StatementData data) {
        List<String> lines = new ArrayList<>();
        Customer cust = data.getCustomer();
        Account acct = data.getAccount();

        // HTML document header (HTML-L01 through HTML-L08)
        lines.add("<!DOCTYPE html>");
        lines.add("<html lang=\"en\">");
        lines.add("<head>");
        lines.add("<meta charset=\"utf-8\">");
        lines.add("<title>HTML Table Layout</title>");
        lines.add("</head>");
        lines.add("<body style=\"margin:0px;\">");
        lines.add("<table  align=\"center\" frame=\"box\" style=\"width:70%; "
                + "font:12px Segoe UI,sans-serif;\">");

        // Bank header row (HTML-L10, L16, L17, L18)
        lines.add("<tr>");
        lines.add("<td colspan=\"3\" style=\"padding:0px 5px;"
                + "background-color:#1d1d96b3;\">");

        // Account number heading (HTML-L11)
        String acctId = trimField(acct.getAcctId());
        lines.add("<h3>Statement for Account Number: " + acctId + "</h3>");
        lines.add("</td>");
        lines.add("</tr>");

        // Bank address row (HTML-L15, L16, L17, L18)
        lines.add("<tr>");
        lines.add("<td colspan=\"3\" style=\"padding:0px 5px;"
                + "background-color:#FFAF33;\">");
        lines.add("<p style=\"font-size:16px\">Bank of XYZ</p>");
        lines.add("<p>410 Terry Ave N</p>");
        lines.add("<p>Seattle WA 99999</p>");
        lines.add("</td>");
        lines.add("</tr>");

        // Customer name and address (HTML-L22-35 block)
        lines.add("<tr>");
        lines.add("<td colspan=\"3\" style=\"padding:0px 5px;"
                + "background-color:#f2f2f2;\">");

        String fullName = buildCustomerName(
                cust.getFirstName(), cust.getMiddleName(), cust.getLastName());
        lines.add("<p style=\"font-size:16px\">" + fullName + "</p>");
        lines.add("<p>" + trimField(cust.getAddrLine1()) + "</p>");
        lines.add("<p>" + trimField(cust.getAddrLine2()) + "</p>");
        lines.add("<p>" + buildAddressLine3(cust) + "</p>");
        lines.add("</td>");
        lines.add("</tr>");

        // Basic Details header (HTML-L30-42, L31)
        lines.add("<tr>");
        lines.add("<td colspan=\"3\" style=\"padding:0px 5px;"
                + "background-color:#33FFD1; text-align:center;\">");
        lines.add("<p style=\"font-size:16px\">Basic Details</p>");
        lines.add("</td>");
        lines.add("</tr>");

        // Basic details content (HTML-L22-35 block)
        lines.add("<tr>");
        lines.add("<td colspan=\"3\" style=\"padding:0px 5px;"
                + "background-color:#f2f2f2;\">");
        lines.add("<p>Account ID         : " + acctId + "</p>");
        lines.add("<p>Current Balance    : " + formatBalance(acct.getCurrBal()) + "</p>");
        lines.add("<p>FICO Score         : " + cust.getFicoCreditScore() + "</p>");
        lines.add("</td>");
        lines.add("</tr>");

        // Transaction Summary header (HTML-L30-42, L43)
        lines.add("<tr>");
        lines.add("<td colspan=\"3\" style=\"padding:0px 5px;"
                + "background-color:#33FFD1; text-align:center;\">");
        lines.add("<p style=\"font-size:16px\">Transaction Summary</p>");
        lines.add("</td>");
        lines.add("</tr>");

        // Transaction column headers (HTML-L47/L48, L50/L51, L53/L54)
        lines.add("<tr>");
        lines.add("<td style=\"width:25%; padding:0px 5px; "
                + "background-color:#33FF5E; text-align:left;\">");
        lines.add("<p style=\"font-size:16px\">Tran ID</p>");
        lines.add("</td>");
        lines.add("<td style=\"width:55%; padding:0px 5px; "
                + "background-color:#33FF5E; text-align:left;\">");
        lines.add("<p style=\"font-size:16px\">Tran Details</p>");
        lines.add("</td>");
        lines.add("<td style=\"width:20%; padding:0px 5px; "
                + "background-color:#33FF5E; text-align:right;\">");
        lines.add("<p style=\"font-size:16px\">Amount</p>");
        lines.add("</td>");
        lines.add("</tr>");

        // Transaction detail rows (HTML-L58/L61/L64 per transaction)
        for (TransactionRecord txn : data.getTransactions()) {
            lines.add("<tr>");
            lines.add("<td style=\"width:25%; padding:0px 5px; "
                    + "background-color:#f2f2f2; text-align:left;\">");
            lines.add("<p>" + trimField(txn.getTranId()) + "</p>");
            lines.add("</td>");
            lines.add("<td style=\"width:55%; padding:0px 5px; "
                    + "background-color:#f2f2f2; text-align:left;\">");
            lines.add("<p>" + trimField(txn.getDescription()) + "</p>");
            lines.add("</td>");
            lines.add("<td style=\"width:20%; padding:0px 5px; "
                    + "background-color:#f2f2f2; text-align:right;\">");
            lines.add("<p>" + formatAmount(txn.getAmount()) + "</p>");
            lines.add("</td>");
            lines.add("</tr>");
        }

        // End of statement footer (HTML-L10, L75)
        lines.add("<tr>");
        lines.add("<td colspan=\"3\" style=\"padding:0px 5px;"
                + "background-color:#1d1d96b3;\">");
        lines.add("<h3>End of Statement</h3>");
        lines.add("</td>");
        lines.add("</tr>");

        // Close table and document (HTML-L78, L79, L80)
        lines.add("</table>");
        lines.add("</body>");
        lines.add("</html>");

        return lines;
    }

    String buildCustomerName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        appendTrimmed(sb, first);
        sb.append(' ');
        appendTrimmed(sb, middle);
        sb.append(' ');
        appendTrimmed(sb, last);
        return sb.toString().trim();
    }

    String buildAddressLine3(Customer cust) {
        StringBuilder sb = new StringBuilder();
        appendTrimmed(sb, cust.getAddrLine3());
        sb.append(' ');
        appendTrimmed(sb, cust.getAddrStateCd());
        sb.append(' ');
        appendTrimmed(sb, cust.getAddrCountryCd());
        sb.append(' ');
        appendTrimmed(sb, cust.getAddrZip());
        return sb.toString().trim();
    }

    String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount);
    }

    String formatBalance(BigDecimal balance) {
        if (balance == null) {
            return "0.00";
        }
        return String.format("%.2f", balance);
    }

    private static String trimField(String s) {
        return s == null ? "" : s.trim();
    }

    private void appendTrimmed(StringBuilder sb, String value) {
        if (value != null) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                sb.append(trimmed);
            }
        }
    }
}
