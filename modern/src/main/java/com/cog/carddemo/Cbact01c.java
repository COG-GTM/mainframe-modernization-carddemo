package com.cog.carddemo;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern equivalent of the batch program {@code app/cbl/CBACT01C.cbl}: read the
 * account master file sequentially and print every account.
 *
 * <p>The COBOL paragraphs map as follows:
 * <ul>
 *   <li>{@code 0000-ACCTFILE-OPEN} / {@code 9000-ACCTFILE-CLOSE} - {@link #run}</li>
 *   <li>{@code 1000-ACCTFILE-GET-NEXT} - {@link AccountFileReader}</li>
 *   <li>{@code 1100-DISPLAY-ACCT-RECORD} - {@link #displayAccountRecord}</li>
 *   <li>{@code 9910-DISPLAY-IO-STATUS} / {@code 9999-ABEND-PROGRAM} - {@link #abend}</li>
 * </ul>
 *
 * <p>Amount fields are {@link java.math.BigDecimal}: the {@code S9(10)V99} scale of
 * two is preserved end to end and no value ever passes through a floating point type.
 */
public final class Cbact01c {

    private static final String SEPARATOR = "-------------------------------------------------";

    /** Width of the label column of {@code 1100-DISPLAY-ACCT-RECORD}. */
    private static final int LABEL_WIDTH = 24;

    /** File status set by the COBOL runtime when the dataset is not available. */
    private static final String STATUS_OPEN_FAILED = "35";

    /** File status set by the COBOL runtime for a malformed record. */
    private static final String STATUS_READ_FAILED = "04";

    private Cbact01c() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: Cbact01c <acctfile>");
            System.exit(2);
        }
        try {
            run(Path.of(args[0]), System.out);
        } catch (AbendException e) {
            System.out.flush();
            System.exit(AbendException.ABEND_CODE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Run the batch job over {@code acctFile}, writing the report to {@code out}.
     *
     * @return the accounts read
     */
    public static List<AccountRecord> run(Path acctFile, PrintStream out) throws IOException {
        line(out, "START OF EXECUTION OF PROGRAM CBACT01C");

        if (!Files.isReadable(acctFile)) {
            line(out, "ERROR OPENING ACCTFILE");
            abend(out, STATUS_OPEN_FAILED, "unable to open " + acctFile);
        }

        List<String> rawRecords = AccountFileReader.readRawRecords(acctFile);
        List<AccountRecord> accounts = new ArrayList<>(rawRecords.size());
        for (String raw : rawRecords) {
            AccountRecord account;
            try {
                account = AccountRecord.parse(raw);
            } catch (IllegalArgumentException e) {
                line(out, "ERROR READING ACCOUNT FILE");
                abend(out, STATUS_READ_FAILED, e.getMessage());
                throw new AssertionError("unreachable");
            }
            accounts.add(account);
            displayAccountRecord(account, out);
            line(out, account.toRawRecord());
        }

        line(out, "END OF EXECUTION OF PROGRAM CBACT01C");
        return accounts;
    }

    /** {@code 1100-DISPLAY-ACCT-RECORD}. */
    static void displayAccountRecord(AccountRecord account, PrintStream out) {
        display(out, "ACCT-ID", account.acctId());
        display(out, "ACCT-ACTIVE-STATUS", account.activeStatus());
        display(out, "ACCT-CURR-BAL", AccountRecord.display(account.currBal()));
        display(out, "ACCT-CREDIT-LIMIT", AccountRecord.display(account.creditLimit()));
        display(out, "ACCT-CASH-CREDIT-LIMIT", AccountRecord.display(account.cashCreditLimit()));
        display(out, "ACCT-OPEN-DATE", account.openDate());
        display(out, "ACCT-EXPIRAION-DATE", account.expirationDate());
        display(out, "ACCT-REISSUE-DATE", account.reissueDate());
        display(out, "ACCT-CURR-CYC-CREDIT", AccountRecord.display(account.currCycCredit()));
        display(out, "ACCT-CURR-CYC-DEBIT", AccountRecord.display(account.currCycDebit()));
        display(out, "ACCT-GROUP-ID", account.groupId());
        line(out, SEPARATOR);
    }

    private static void display(PrintStream out, String label, String value) {
        line(out, label + " ".repeat(LABEL_WIDTH - label.length()) + ":" + value);
    }

    /** Terminate every report line with a newline regardless of the host platform. */
    private static void line(PrintStream out, String text) {
        out.print(text);
        out.print('\n');
    }

    /** {@code 9910-DISPLAY-IO-STATUS} followed by {@code 9999-ABEND-PROGRAM}. */
    private static void abend(PrintStream out, String fileStatus, String detail) {
        line(out, "FILE STATUS IS: NNNN00" + fileStatus);
        line(out, "ABENDING PROGRAM");
        throw new AbendException(detail, fileStatus);
    }
}
