package com.carddemo.batch.statement;

import java.math.BigDecimal;
import java.util.List;

/**
 * COBOL program: CBSTM03A — one account statement as written to the STMTFILE (fixed 80 byte
 * records) and HTMLFILE (fixed 100 byte records) DD names of CREASTMT.JCL.
 *
 * @param cardNumber   XREF-CARD-NUM of the statement's cross-reference record
 * @param accountId    XREF-ACCT-ID / ACCT-ID the statement is printed for
 * @param textLines    STMTFILE records, each exactly 80 characters
 * @param htmlLines    HTMLFILE records, each exactly 100 characters
 * @param totalAmount  WS-TOTAL-AMT, the sum of TRNX-AMT over the card's transactions
 */
public record StatementDocument(String cardNumber,
                                Long accountId,
                                List<String> textLines,
                                List<String> htmlLines,
                                BigDecimal totalAmount) {

    /** The STMTFILE records as a single printable document. */
    public String text() {
        return join(textLines);
    }

    /** The HTMLFILE records as a single printable document. */
    public String html() {
        return join(htmlLines);
    }

    private static String join(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
