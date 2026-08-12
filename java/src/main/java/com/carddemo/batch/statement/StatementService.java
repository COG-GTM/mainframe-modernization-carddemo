package com.carddemo.batch.statement;

import static com.carddemo.batch.statement.StatementFormatter.alpha;
import static com.carddemo.batch.statement.StatementFormatter.delimited;
import static com.carddemo.batch.statement.StatementFormatter.editedSuppressed;
import static com.carddemo.batch.statement.StatementFormatter.editedZeroFilled;
import static com.carddemo.batch.statement.StatementFormatter.fill;
import static com.carddemo.batch.statement.StatementFormatter.numericToAlpha;
import static com.carddemo.batch.statement.StatementFormatter.spaces;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.model.entity.Transaction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: CBSTM03A — prints an account statement per CARDXREF record in plain text
 * (STMTFILE, 80 byte records) and HTML (HTMLFILE, 100 byte records). Replaces CREASTMT.JCL
 * step STEP040; the file I/O of its CBSTM03B subroutine lives in {@link StatementFileService}.
 *
 * <p>Copybooks: COSTM01 (CUSTREC / TRNX-RECORD statement work areas), CVACT03Y (CARD-XREF-RECORD),
 * CVCUS01Y (CUSTOMER-RECORD) and CVACT01Y (ACCOUNT-RECORD). The STATEMENT-LINES and HTML-LINES
 * group items of the COBOL working storage are reproduced field by field so that both outputs
 * stay byte identical to the mainframe listing.
 */
@Service
public class StatementService {

    /** LRECL of the STMTFILE DD. */
    static final int TEXT_RECORD_LENGTH = 80;

    /** LRECL of the HTMLFILE DD. */
    static final int HTML_RECORD_LENGTH = 100;

    // STATEMENT-LINES entries that carry only FILLER ... VALUE literals.
    private static final String ST_LINE0 = fill('*', 31) + "START OF STATEMENT" + fill('*', 31);
    private static final String ST_LINE_RULE = fill('-', 80);
    private static final String ST_LINE6 = spaces(33) + alpha("Basic Details", 14) + spaces(33);
    private static final String ST_LINE11 = spaces(30) + "TRANSACTION SUMMARY " + spaces(30);
    private static final String ST_LINE13 =
            "Tran ID         " + alpha("Tran Details    ", 51) + "  Tran Amount";
    private static final String ST_LINE15 = fill('*', 32) + "END OF STATEMENT" + fill('*', 32);

    // HTML-LINES 88-level condition values, in COBOL declaration order.
    private static final String HTML_L01 = "<!DOCTYPE html>";
    private static final String HTML_L02 = "<html lang=\"en\">";
    private static final String HTML_L03 = "<head>";
    private static final String HTML_L04 = "<meta charset=\"utf-8\">";
    private static final String HTML_L05 = "<title>HTML Table Layout</title>";
    private static final String HTML_L06 = "</head>";
    private static final String HTML_L07 = "<body style=\"margin:0px;\">";
    private static final String HTML_L08 =
            "<table  align=\"center\" frame=\"box\" style=\"width:70%; font:12px Segoe UI,sans-serif;\">";
    private static final String HTML_LTRS = "<tr>";
    private static final String HTML_LTRE = "</tr>";
    private static final String HTML_LTDE = "</td>";
    private static final String HTML_L10 =
            "<td colspan=\"3\" style=\"padding:0px 5px;background-color:#1d1d96b3;\">";
    private static final String HTML_L15 =
            "<td colspan=\"3\" style=\"padding:0px 5px;background-color:#FFAF33;\">";
    private static final String HTML_L16 = "<p style=\"font-size:16px\">Bank of XYZ</p>";
    private static final String HTML_L17 = "<p>410 Terry Ave N</p>";
    private static final String HTML_L18 = "<p>Seattle WA 99999</p>";
    private static final String HTML_L22_35 =
            "<td colspan=\"3\" style=\"padding:0px 5px;background-color:#f2f2f2;\">";
    private static final String HTML_L30_42 =
            "<td colspan=\"3\" style=\"padding:0px 5px;background-color:#33FFD1; text-align:center;\">";
    private static final String HTML_L31 = "<p style=\"font-size:16px\">Basic Details</p>";
    private static final String HTML_L43 = "<p style=\"font-size:16px\">Transaction Summary</p>";
    private static final String HTML_L47 =
            "<td style=\"width:25%; padding:0px 5px; background-color:#33FF5E; text-align:left;\">";
    private static final String HTML_L48 = "<p style=\"font-size:16px\">Tran ID</p>";
    private static final String HTML_L50 =
            "<td style=\"width:55%; padding:0px 5px; background-color:#33FF5E; text-align:left;\">";
    private static final String HTML_L51 = "<p style=\"font-size:16px\">Tran Details</p>";
    private static final String HTML_L53 =
            "<td style=\"width:20%; padding:0px 5px; background-color:#33FF5E; text-align:right;\">";
    private static final String HTML_L54 = "<p style=\"font-size:16px\">Amount</p>";
    private static final String HTML_L58 =
            "<td style=\"width:25%; padding:0px 5px; background-color:#f2f2f2; text-align:left;\">";
    private static final String HTML_L61 =
            "<td style=\"width:55%; padding:0px 5px; background-color:#f2f2f2; text-align:left;\">";
    private static final String HTML_L64 =
            "<td style=\"width:20%; padding:0px 5px; background-color:#f2f2f2; text-align:right;\">";
    private static final String HTML_L75 = "<h3>End of Statement</h3>";
    private static final String HTML_L78 = "</table>";
    private static final String HTML_L79 = "</body>";
    private static final String HTML_L80 = "</html>";

    private final StatementFileService fileService;

    public StatementService(StatementFileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 1000-MAINLINE: reads CARDXREF sequentially and prints one statement per cross-reference
     * record, using the transaction table built once from TRNXFILE.
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<StatementDocument> generateStatements() {
        StatementTransactionTable table = StatementTransactionTable.load(fileService.readTransactionFile());
        List<StatementDocument> statements = new ArrayList<>();
        for (CardXref xref : fileService.readXrefFile()) {
            Customer customer = fileService.readCustomer(xref.getCustomerId());
            Account account = fileService.readAccount(xref.getAccountId());
            statements.add(render(xref, customer, account, table.transactionsFor(xref.getCardNumber())));
        }
        return statements;
    }

    /** Prints the statement of a single cross-reference record (5000/4000/6000 paragraphs). */
    public StatementDocument render(CardXref xref,
                                    Customer customer,
                                    Account account,
                                    List<Transaction> transactions) {
        List<String> text = new ArrayList<>();
        List<String> html = new ArrayList<>();

        // 5000-CREATE-STATEMENT.
        text.add(ST_LINE0);
        String accountIdField = numericToAlpha(account.getAccountId(), 11, 20);
        writeHtmlHeader(html, accountIdField);

        String name = delimited(customer.getFirstName(), 25, " ")
                + " " + delimited(customer.getMiddleName(), 25, " ")
                + " " + delimited(customer.getLastName(), 25, " ")
                + " ";
        String stName = alpha(name, 75);
        String stAddress1 = alpha(customer.getAddressLine1(), 50);
        String stAddress2 = alpha(customer.getAddressLine2(), 50);
        String stAddress3 = alpha(delimited(customer.getAddressLine3(), 50, " ")
                + " " + delimited(customer.getStateCode(), 2, " ")
                + " " + delimited(customer.getCountryCode(), 3, " ")
                + " " + delimited(customer.getZipCode(), 10, " ")
                + " ", 80);
        String currentBalance = editedZeroFilled(account.getCurrentBalance());
        String ficoScore = numericToAlpha(customer.getFicoCreditScore(), 3, 20);

        writeHtmlNameAddressBasics(html, stName, stAddress1, stAddress2, stAddress3,
                accountIdField, currentBalance, ficoScore);

        text.add(stName + spaces(5));
        text.add(stAddress1 + spaces(30));
        text.add(stAddress2 + spaces(30));
        text.add(stAddress3);
        text.add(ST_LINE_RULE);
        text.add(ST_LINE6);
        text.add(ST_LINE_RULE);
        text.add("Account ID         :" + accountIdField + spaces(40));
        text.add("Current Balance    :" + currentBalance + spaces(7) + spaces(40));
        text.add("FICO Score         :" + ficoScore + spaces(40));
        text.add(ST_LINE_RULE);
        text.add(ST_LINE11);
        text.add(ST_LINE_RULE);
        text.add(ST_LINE13);
        text.add(ST_LINE_RULE);

        // 4000-TRNXFILE-GET, printing each transaction through 6000-WRITE-TRANS.
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            writeTransaction(text, html, transaction);
            total = total.add(transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount());
        }

        text.add(ST_LINE_RULE);
        text.add("Total EXP:" + spaces(56) + "$" + editedSuppressed(total));
        text.add(ST_LINE15);

        html.add(HTML_LTRS);
        html.add(HTML_L10);
        html.add(HTML_L75);
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
        html.add(HTML_L78);
        html.add(HTML_L79);
        html.add(HTML_L80);

        return new StatementDocument(xref.getCardNumber(), account.getAccountId(),
                pad(text, TEXT_RECORD_LENGTH), pad(html, HTML_RECORD_LENGTH), total);
    }

    /** 5100-WRITE-HTML-HEADER. */
    private void writeHtmlHeader(List<String> html, String accountIdField) {
        html.add(HTML_L01);
        html.add(HTML_L02);
        html.add(HTML_L03);
        html.add(HTML_L04);
        html.add(HTML_L05);
        html.add(HTML_L06);
        html.add(HTML_L07);
        html.add(HTML_L08);
        html.add(HTML_LTRS);
        html.add(HTML_L10);
        html.add("<h3>Statement for Account Number: " + accountIdField + "</h3>");
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
        html.add(HTML_LTRS);
        html.add(HTML_L15);
        html.add(HTML_L16);
        html.add(HTML_L17);
        html.add(HTML_L18);
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
        html.add(HTML_LTRS);
        html.add(HTML_L22_35);
    }

    /** 5200-WRITE-HTML-NMADBS. */
    private void writeHtmlNameAddressBasics(List<String> html,
                                            String stName,
                                            String stAddress1,
                                            String stAddress2,
                                            String stAddress3,
                                            String accountIdField,
                                            String currentBalance,
                                            String ficoScore) {
        html.add("<p style=\"font-size:16px\">" + delimited(stName, 50, "  ") + "  </p>");
        html.add("<p>" + delimited(stAddress1, 50, "  ") + "  </p>");
        html.add("<p>" + delimited(stAddress2, 50, "  ") + "  </p>");
        html.add("<p>" + delimited(stAddress3, 80, "  ") + "  </p>");
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
        html.add(HTML_LTRS);
        html.add(HTML_L30_42);
        html.add(HTML_L31);
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
        html.add(HTML_LTRS);
        html.add(HTML_L22_35);
        html.add("<p>Account ID         : " + accountIdField + "</p>");
        html.add("<p>Current Balance    : " + currentBalance + "</p>");
        html.add("<p>FICO Score         : " + ficoScore + "</p>");
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
        html.add(HTML_LTRS);
        html.add(HTML_L30_42);
        html.add(HTML_L43);
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
        html.add(HTML_LTRS);
        html.add(HTML_L47);
        html.add(HTML_L48);
        html.add(HTML_LTDE);
        html.add(HTML_L50);
        html.add(HTML_L51);
        html.add(HTML_LTDE);
        html.add(HTML_L53);
        html.add(HTML_L54);
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
    }

    /** 6000-WRITE-TRANS. */
    private void writeTransaction(List<String> text, List<String> html, Transaction transaction) {
        String tranId = alpha(transaction.getTransactionId(), 16);
        String tranDetails = alpha(alpha(transaction.getDescription(), 100), 49);
        String tranAmount = editedSuppressed(transaction.getAmount());

        text.add(tranId + " " + tranDetails + "$" + tranAmount);

        html.add(HTML_LTRS);
        html.add(HTML_L58);
        html.add("<p>" + tranId + "</p>");
        html.add(HTML_LTDE);
        html.add(HTML_L61);
        html.add("<p>" + tranDetails + "</p>");
        html.add(HTML_LTDE);
        html.add(HTML_L64);
        html.add("<p>" + tranAmount + "</p>");
        html.add(HTML_LTDE);
        html.add(HTML_LTRE);
    }

    /** WRITE of a record area of fixed length: every record is padded out to the LRECL. */
    private static List<String> pad(List<String> lines, int length) {
        List<String> records = new ArrayList<>(lines.size());
        for (String line : lines) {
            records.add(alpha(line, length));
        }
        return records;
    }
}
