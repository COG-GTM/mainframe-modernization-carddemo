package com.carddemo.service;

import com.carddemo.config.CardDemoProperties;
import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CBSTM03A - CREASTMT: produces the plain text and HTML statements for one card cross reference
 * record, joining customer, account and transaction data exactly as the COBOL program did.
 */
@Service
public class StatementService {

    private final CardXrefRepository xrefs;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final CardDemoProperties properties;

    public StatementService(CardXrefRepository xrefs,
                            CustomerRepository customers,
                            AccountRepository accounts,
                            TransactionRepository transactions,
                            CardDemoProperties properties) {
        this.xrefs = xrefs;
        this.customers = customers;
        this.accounts = accounts;
        this.transactions = transactions;
        this.properties = properties;
    }

    /** Rendered statement for one card. */
    public record Statement(String cardNumber, String text, String html, BigDecimal total) {
    }

    @Transactional(readOnly = true)
    public List<Statement> generateAll() {
        return xrefs.findAll().stream().map(this::generate).filter(java.util.Objects::nonNull).toList();
    }

    @Transactional(readOnly = true)
    public Statement generate(CardXref xref) {
        Customer customer = customers.findById(xref.getCustomerId()).orElse(null);
        Account account = accounts.findById(xref.getAccountId()).orElse(null);
        if (customer == null || account == null) {
            return null;
        }
        List<Transaction> cardTransactions =
                transactions.findByCardNumberOrderByIdAsc(xref.getCardNumber());
        BigDecimal total = cardTransactions.stream()
                .map(transaction -> transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Statement(
                xref.getCardNumber(),
                text(customer, account, cardTransactions, total),
                html(customer, account, cardTransactions, total),
                total);
    }

    /** Writes every statement to the configured output directory, as CREASTMT wrote its GDGs. */
    public Path writeAll() {
        StringBuilder text = new StringBuilder();
        StringBuilder html = new StringBuilder("<html><body>");
        for (Statement statement : generateAll()) {
            text.append(statement.text()).append(System.lineSeparator());
            html.append(statement.html());
        }
        html.append("</body></html>");
        write("statements.html", html.toString());
        return write("statements.txt", text.toString());
    }

    private String text(Customer customer, Account account, List<Transaction> rows, BigDecimal total) {
        StringBuilder builder = new StringBuilder();
        builder.append("*".repeat(80)).append(System.lineSeparator());
        builder.append("            STATEMENT SUMMARY").append(System.lineSeparator());
        builder.append("*".repeat(80)).append(System.lineSeparator());
        builder.append("Name      : ")
                .append(join(customer.getFirstName(), customer.getMiddleName(), customer.getLastName()))
                .append(System.lineSeparator());
        builder.append("Address   : ").append(nullSafe(customer.getAddressLine1())).append(System.lineSeparator());
        builder.append("            ").append(nullSafe(customer.getAddressLine2())).append(System.lineSeparator());
        builder.append("            ")
                .append(join(customer.getAddressLine3(), customer.getStateCode(), customer.getZipCode()))
                .append(System.lineSeparator());
        builder.append("Account ID: ").append(account.getId()).append(System.lineSeparator());
        builder.append("Curr Bal  : ").append(nz(account.getCurrentBalance()).toPlainString())
                .append(System.lineSeparator());
        builder.append("FICO Score: ").append(customer.getFicoCreditScore()).append(System.lineSeparator());
        builder.append("-".repeat(80)).append(System.lineSeparator());
        builder.append(String.format("%-16s %-50s %10s%n", "TRANSACTION ID", "DESCRIPTION", "AMOUNT"));
        for (Transaction transaction : rows) {
            builder.append(String.format("%-16s %-50s %10s%n",
                    transaction.getId(),
                    nullSafe(transaction.getDescription()),
                    nz(transaction.getAmount()).toPlainString()));
        }
        builder.append(String.format("%-67s %10s%n", "TOTAL", total.toPlainString()));
        return builder.toString();
    }

    private String html(Customer customer, Account account, List<Transaction> rows, BigDecimal total) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h2>Statement Summary</h2><table>");
        builder.append(row("Name", join(customer.getFirstName(), customer.getMiddleName(), customer.getLastName())));
        builder.append(row("Address", join(customer.getAddressLine1(), customer.getAddressLine2(),
                customer.getAddressLine3(), customer.getStateCode(), customer.getZipCode())));
        builder.append(row("Account ID", String.valueOf(account.getId())));
        builder.append(row("Current Balance", nz(account.getCurrentBalance()).toPlainString()));
        builder.append(row("FICO Score", String.valueOf(customer.getFicoCreditScore())));
        builder.append("</table><table><tr><th>Transaction ID</th><th>Description</th><th>Amount</th></tr>");
        for (Transaction transaction : rows) {
            builder.append("<tr><td>").append(escape(nullSafe(transaction.getId()))).append("</td><td>")
                    .append(escape(nullSafe(transaction.getDescription()))).append("</td><td>")
                    .append(nz(transaction.getAmount()).toPlainString()).append("</td></tr>");
        }
        builder.append("<tr><td colspan=\"2\">Total</td><td>").append(total.toPlainString())
                .append("</td></tr></table>");
        return builder.toString();
    }

    private Path write(String fileName, String content) {
        try {
            Path directory = Path.of(properties.getOutputDirectory());
            Files.createDirectories(directory);
            Path file = directory.resolve(fileName);
            Files.writeString(file, content);
            return file;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String row(String label, String value) {
        return "<tr><td>" + escape(label) + "</td><td>" + escape(value) + "</td></tr>";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String join(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            String trimmed = nullSafe(value).trim();
            if (!trimmed.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(trimmed);
            }
        }
        return builder.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
