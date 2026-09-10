package com.carddemo.batch.readers;

import com.carddemo.model.entity.Account;
import java.util.List;

/**
 * COBOL program: CBACT01C — read and print the account master file (JCL READACCT).
 * Copybook CVACT01Y (ACCOUNT-RECORD, RECLN 300), VSAM file ACCTDATA.
 *
 * <p>Produces, per record, the eleven labelled lines of {@code 1100-DISPLAY-ACCT-RECORD}
 * followed by the separator line and the raw {@code DISPLAY ACCOUNT-RECORD} of the main
 * read loop, in that order.
 */
final class Cbact01cDisplay {

    private static final String SEPARATOR = "-".repeat(49);

    private Cbact01cDisplay() {
    }

    /** {@code 1100-DISPLAY-ACCT-RECORD} followed by the group-level DISPLAY of the record. */
    static List<String> displayLines(Account account) {
        return List.of(
                "ACCT-ID                 :" + CobolDisplay.number(account.getAccountId(), 11),
                "ACCT-ACTIVE-STATUS      :" + CobolDisplay.text(account.getActiveStatus(), 1),
                "ACCT-CURR-BAL           :" + CobolDisplay.zoned(account.getCurrentBalance(), 10, 2),
                "ACCT-CREDIT-LIMIT       :" + CobolDisplay.zoned(account.getCreditLimit(), 10, 2),
                "ACCT-CASH-CREDIT-LIMIT  :" + CobolDisplay.zoned(account.getCashCreditLimit(), 10, 2),
                "ACCT-OPEN-DATE          :" + CobolDisplay.text(account.getOpenDate(), 10),
                "ACCT-EXPIRAION-DATE     :" + CobolDisplay.text(account.getExpirationDate(), 10),
                "ACCT-REISSUE-DATE       :" + CobolDisplay.text(account.getReissueDate(), 10),
                "ACCT-CURR-CYC-CREDIT    :" + CobolDisplay.zoned(account.getCurrentCycleCredit(), 10, 2),
                "ACCT-CURR-CYC-DEBIT     :" + CobolDisplay.zoned(account.getCurrentCycleDebit(), 10, 2),
                "ACCT-GROUP-ID           :" + CobolDisplay.text(account.getGroupId(), 10),
                SEPARATOR,
                record(account));
    }

    /** The 300 byte ACCOUNT-RECORD as laid out by CVACT01Y, trailing FILLER included. */
    static String record(Account account) {
        return CobolDisplay.number(account.getAccountId(), 11)
                + CobolDisplay.text(account.getActiveStatus(), 1)
                + CobolDisplay.zoned(account.getCurrentBalance(), 10, 2)
                + CobolDisplay.zoned(account.getCreditLimit(), 10, 2)
                + CobolDisplay.zoned(account.getCashCreditLimit(), 10, 2)
                + CobolDisplay.text(account.getOpenDate(), 10)
                + CobolDisplay.text(account.getExpirationDate(), 10)
                + CobolDisplay.text(account.getReissueDate(), 10)
                + CobolDisplay.zoned(account.getCurrentCycleCredit(), 10, 2)
                + CobolDisplay.zoned(account.getCurrentCycleDebit(), 10, 2)
                + CobolDisplay.text(account.getAddressZip(), 10)
                + CobolDisplay.text(account.getGroupId(), 10)
                + " ".repeat(178);
    }
}
