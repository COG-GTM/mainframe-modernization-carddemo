package com.carddemo.batch.statement;

import com.carddemo.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

/**
 * Statement writer — generates plain text and HTML statement files.
 * Replaces DD-name-based output from CBSTM03A/B with configurable file paths.
 */
public class StatementWriter implements ItemWriter<StatementData> {

    private static final Logger log = LoggerFactory.getLogger(StatementWriter.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String outputDir;

    public StatementWriter(String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void write(Chunk<? extends StatementData> statements) throws Exception {
        for (StatementData stmt : statements) {
            writeTextStatement(stmt);
            writeHtmlStatement(stmt);
        }
    }

    private void writeTextStatement(StatementData stmt) throws IOException {
        String filename = outputDir + "/statement_" + stmt.getAccount().getAcctId() + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("==========================================================");
            pw.println("                  CREDIT CARD STATEMENT");
            pw.println("==========================================================");
            pw.printf("Account:       %d%n", stmt.getAccount().getAcctId());
            if (stmt.getCustomer() != null) {
                pw.printf("Customer:      %s %s%n", stmt.getCustomer().getFirstName(), stmt.getCustomer().getLastName());
            }
            pw.printf("Period:        %s to %s%n", stmt.getPeriodStart().format(DATE_FMT), stmt.getPeriodEnd().format(DATE_FMT));
            pw.printf("Balance:       $%,.2f%n", stmt.getAccount().getCurrentBalance());
            pw.printf("Credit Limit:  $%,.2f%n", stmt.getAccount().getCreditLimit());
            pw.println("----------------------------------------------------------");
            pw.printf("%-16s %-10s %-30s %12s%n", "DATE", "TYPE", "DESCRIPTION", "AMOUNT");
            pw.println("----------------------------------------------------------");

            for (Transaction t : stmt.getTransactions()) {
                String date = t.getOrigTimestamp() != null ? t.getOrigTimestamp().format(TS_FMT) : "";
                pw.printf("%-16s %-10s %-30s %12.2f%n",
                        date.length() > 16 ? date.substring(0, 16) : date,
                        t.getTypeCd(),
                        t.getDescription() != null ? (t.getDescription().length() > 30 ? t.getDescription().substring(0, 30) : t.getDescription()) : "",
                        t.getAmount());
            }

            pw.println("----------------------------------------------------------");
            pw.printf("Total Debits:  $%,.2f%n", stmt.getTotalDebits());
            pw.printf("Total Credits: $%,.2f%n", stmt.getTotalCredits());
            pw.println("==========================================================");
        }
        log.info("Generated text statement for account {}", stmt.getAccount().getAcctId());
    }

    private void writeHtmlStatement(StatementData stmt) throws IOException {
        String filename = outputDir + "/statement_" + stmt.getAccount().getAcctId() + ".html";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("<!DOCTYPE html><html><head><title>Statement</title>");
            pw.println("<style>table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:8px;text-align:left}th{background:#4CAF50;color:white}.total{font-weight:bold}</style>");
            pw.println("</head><body>");
            pw.printf("<h1>Credit Card Statement</h1>%n");
            pw.printf("<p>Account: %d</p>%n", stmt.getAccount().getAcctId());
            if (stmt.getCustomer() != null) {
                pw.printf("<p>Customer: %s %s</p>%n", stmt.getCustomer().getFirstName(), stmt.getCustomer().getLastName());
            }
            pw.printf("<p>Period: %s to %s</p>%n", stmt.getPeriodStart().format(DATE_FMT), stmt.getPeriodEnd().format(DATE_FMT));
            pw.printf("<p>Balance: $%,.2f | Credit Limit: $%,.2f</p>%n",
                    stmt.getAccount().getCurrentBalance(), stmt.getAccount().getCreditLimit());
            pw.println("<table><tr><th>Date</th><th>Type</th><th>Description</th><th>Amount</th></tr>");

            for (Transaction t : stmt.getTransactions()) {
                pw.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>$%,.2f</td></tr>%n",
                        t.getOrigTimestamp() != null ? t.getOrigTimestamp().format(TS_FMT) : "",
                        t.getTypeCd(),
                        t.getDescription() != null ? t.getDescription() : "",
                        t.getAmount());
            }

            pw.printf("<tr class='total'><td colspan='3'>Total Debits</td><td>$%,.2f</td></tr>%n", stmt.getTotalDebits());
            pw.printf("<tr class='total'><td colspan='3'>Total Credits</td><td>$%,.2f</td></tr>%n", stmt.getTotalCredits());
            pw.println("</table></body></html>");
        }
        log.info("Generated HTML statement for account {}", stmt.getAccount().getAcctId());
    }
}
