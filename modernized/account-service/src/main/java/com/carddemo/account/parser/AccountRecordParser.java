package com.carddemo.account.parser;

import com.carddemo.account.model.Account;
import org.springframework.stereotype.Component;

/**
 * Parses a fixed-width 300-byte {@code ACCTDAT} record (copybook {@code CVACT01Y})
 * into an {@link Account}. Byte offsets are derived directly from the copybook PIC
 * clauses:
 *
 * <pre>
 *   ACCT-ID                PIC 9(11)       [  0:11]
 *   ACCT-ACTIVE-STATUS     PIC X(01)       [ 11:12]
 *   ACCT-CURR-BAL          PIC S9(10)V99   [ 12:24]
 *   ACCT-CREDIT-LIMIT      PIC S9(10)V99   [ 24:36]
 *   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99   [ 36:48]
 *   ACCT-OPEN-DATE         PIC X(10)       [ 48:58]
 *   ACCT-EXPIRAION-DATE    PIC X(10)       [ 58:68]
 *   ACCT-REISSUE-DATE      PIC X(10)       [ 68:78]
 *   ACCT-CURR-CYC-CREDIT   PIC S9(10)V99   [ 78:90]
 *   ACCT-CURR-CYC-DEBIT    PIC S9(10)V99   [ 90:102]
 *   ACCT-ADDR-ZIP          PIC X(10)       [102:112]
 *   ACCT-GROUP-ID          PIC X(10)       [112:122]
 *   FILLER                 PIC X(178)      [122:300]
 * </pre>
 */
@Component
public class AccountRecordParser {

    public static final int RECORD_LENGTH = 300;
    private static final int MONEY_DECIMALS = 2;

    public Account parse(String record) {
        if (record == null) {
            throw new IllegalArgumentException("Account record must not be null");
        }
        // Pad short lines so a record that was right-trimmed still maps cleanly.
        String r = record.length() < RECORD_LENGTH
                ? String.format("%-" + RECORD_LENGTH + "s", record)
                : record;
        if (r.length() != RECORD_LENGTH) {
            throw new IllegalArgumentException(
                    "ACCTDAT record must be " + RECORD_LENGTH + " bytes, got " + r.length());
        }

        return new Account(
                r.substring(0, 11).trim(),
                r.substring(11, 12).trim(),
                ZonedDecimal.decode(r.substring(12, 24), MONEY_DECIMALS),
                ZonedDecimal.decode(r.substring(24, 36), MONEY_DECIMALS),
                ZonedDecimal.decode(r.substring(36, 48), MONEY_DECIMALS),
                r.substring(48, 58).trim(),
                r.substring(58, 68).trim(),
                r.substring(68, 78).trim(),
                ZonedDecimal.decode(r.substring(78, 90), MONEY_DECIMALS),
                ZonedDecimal.decode(r.substring(90, 102), MONEY_DECIMALS),
                r.substring(102, 112).trim(),
                r.substring(112, 122).trim());
    }
}
