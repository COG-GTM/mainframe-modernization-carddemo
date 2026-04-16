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
 * Generates plain text statements matching the COBOL STATEMENT-LINES structure
 * from CBSTM03A (sections 5000-CREATE-STATEMENT and 6000-WRITE-TRANS).
 */
@Component
public class PlainTextStatementFormatter {

    private static final int LINE_WIDTH = 80;
    private static final String STARS = "*".repeat(LINE_WIDTH);
    private static final String DASHES = "-".repeat(LINE_WIDTH);

    public List<String> format(StatementData data) {
        List<String> lines = new ArrayList<>();
        Customer cust = data.getCustomer();
        Account acct = data.getAccount();

        // ST-LINE0: start of statement banner
        lines.add(buildBanner("START OF STATEMENT"));

        // ST-LINE1: customer name
        String fullName = buildCustomerName(
                cust.getFirstName(), cust.getMiddleName(), cust.getLastName());
        lines.add(padRight(fullName, LINE_WIDTH));

        // ST-LINE2: address line 1
        lines.add(padRight(trimField(cust.getAddrLine1()), LINE_WIDTH));

        // ST-LINE3: address line 2
        lines.add(padRight(trimField(cust.getAddrLine2()), LINE_WIDTH));

        // ST-LINE4: address line 3 + state + country + zip
        String addr3 = buildAddressLine3(cust);
        lines.add(padRight(addr3, LINE_WIDTH));

        // ST-LINE5: separator
        lines.add(DASHES);

        // ST-LINE6: 'Basic Details' centered
        lines.add(center("Basic Details", LINE_WIDTH));

        // ST-LINE5 again: separator
        lines.add(DASHES);

        // ST-LINE7: Account ID
        lines.add(padRight("Account ID         :" + padRight(trimField(acct.getAcctId()), 20), LINE_WIDTH));

        // ST-LINE8: Current Balance (PIC 9(9).99-)
        lines.add(padRight("Current Balance    :" + formatBalance(acct.getCurrBal()), LINE_WIDTH));

        // ST-LINE9: FICO Score
        lines.add(padRight("FICO Score         :" + padRight(String.valueOf(cust.getFicoCreditScore()), 20), LINE_WIDTH));

        // ST-LINE10: separator
        lines.add(DASHES);

        // ST-LINE11: TRANSACTION SUMMARY centered
        lines.add(center("TRANSACTION SUMMARY", LINE_WIDTH));

        // ST-LINE12: separator
        lines.add(DASHES);

        // ST-LINE13: column headers
        lines.add(formatColumnHeaders());

        // ST-LINE12: separator
        lines.add(DASHES);

        // ST-LINE14: transaction detail lines
        for (TransactionRecord txn : data.getTransactions()) {
            lines.add(formatTransactionLine(txn));
        }

        // ST-LINE12: separator before total
        lines.add(DASHES);

        // ST-LINE14A: total line
        lines.add(formatTotalLine(data.getTotalAmount()));

        // ST-LINE15: end of statement banner
        lines.add(buildBanner("END OF STATEMENT"));

        return lines;
    }

    /**
     * Builds the customer full name using COBOL STRING DELIMITED BY ' ' logic.
     */
    String buildCustomerName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        appendTrimmed(sb, first);
        sb.append(' ');
        appendTrimmed(sb, middle);
        sb.append(' ');
        appendTrimmed(sb, last);
        return sb.toString().trim();
    }

    /**
     * Builds address line 3 combining line3, state, country, zip
     * using COBOL STRING DELIMITED BY ' ' logic.
     */
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

    String formatColumnHeaders() {
        // Matches ST-LINE13 layout: Tran ID(16) + Tran Details(51) + Tran Amount(13)
        return padRight("Tran ID         ", 16)
                + padRight("Tran Details    ", 51)
                + "  Tran Amount";
    }

    String formatTransactionLine(TransactionRecord txn) {
        String tranId = padRight(trimField(txn.getTranId()), 16);
        String desc = padRight(trimField(txn.getDescription()), 49);
        String amount = "$" + formatAmount(txn.getAmount());
        return tranId + " " + desc + amount;
    }

    String formatTotalLine(BigDecimal total) {
        String prefix = "Total EXP:";
        String amount = "$" + formatAmount(total);
        int spacesNeeded = LINE_WIDTH - prefix.length() - amount.length();
        if (spacesNeeded < 0) {
            spacesNeeded = 1;
        }
        return prefix + " ".repeat(spacesNeeded) + amount;
    }

    /**
     * Formats a BigDecimal amount to match COBOL PIC Z(9).99- format.
     */
    String formatAmount(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        BigDecimal abs = amount.abs();
        String formatted = String.format("%10.2f", abs);
        if (amount.signum() < 0) {
            formatted = formatted + "-";
        } else {
            formatted = formatted + " ";
        }
        return formatted;
    }

    String formatBalance(BigDecimal balance) {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        BigDecimal abs = balance.abs();
        String formatted = String.format("%9.2f", abs);
        if (balance.signum() < 0) {
            formatted = formatted + "-";
        } else {
            formatted = formatted + " ";
        }
        return formatted;
    }

    String buildBanner(String text) {
        int textLen = text.length();
        int starsEach = (LINE_WIDTH - textLen) / 2;
        return "*".repeat(starsEach) + text + "*".repeat(LINE_WIDTH - starsEach - textLen);
    }

    static String center(String text, int width) {
        if (text == null) text = "";
        int padding = (width - text.length()) / 2;
        if (padding < 0) padding = 0;
        return " ".repeat(padding) + text
                + " ".repeat(width - padding - text.length());
    }

    static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    static String trimField(String s) {
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
