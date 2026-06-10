package com.carddemo.interestcalc.batch;

import com.carddemo.interestcalc.domain.AccountRecord;
import com.carddemo.interestcalc.domain.CardXrefRecord;
import com.carddemo.interestcalc.domain.DisclosureGroupRecord;
import com.carddemo.interestcalc.domain.TranCatBalRecord;
import com.carddemo.interestcalc.util.ZonedDecimal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Parsers for the fixed-width ASCII exports under {@code app/data/ASCII/}, using the
 * byte offsets from the copybooks (see Javadoc on each domain class).
 */
public final class AsciiFileLoader {

    private AsciiFileLoader() {
    }

    /** Parses {@code tcatbal.txt} (CVTRA01Y layout, RECLN 50). */
    public static List<TranCatBalRecord> loadTranCatBal(Path file) {
        return lines(file).stream().map(AsciiFileLoader::parseTranCatBal).toList();
    }

    static TranCatBalRecord parseTranCatBal(String line) {
        return new TranCatBalRecord(
                Long.parseLong(line.substring(0, 11)),            // TRANCAT-ACCT-ID 9(11)   [0,11)
                line.substring(11, 13),                           // TRANCAT-TYPE-CD X(02)   [11,13)
                Integer.parseInt(line.substring(13, 17)),         // TRANCAT-CD      9(04)   [13,17)
                ZonedDecimal.parse(line.substring(17, 28), 2));   // TRAN-CAT-BAL S9(09)V99  [17,28)
    }

    /** Parses {@code discgrp.txt} (CVTRA02Y layout, RECLN 50). */
    public static List<DisclosureGroupRecord> loadDisclosureGroups(Path file) {
        return lines(file).stream().map(AsciiFileLoader::parseDisclosureGroup).toList();
    }

    static DisclosureGroupRecord parseDisclosureGroup(String line) {
        return new DisclosureGroupRecord(
                line.substring(0, 10).trim(),                     // DIS-ACCT-GROUP-ID X(10) [0,10)
                line.substring(10, 12),                           // DIS-TRAN-TYPE-CD  X(02) [10,12)
                Integer.parseInt(line.substring(12, 16)),         // DIS-TRAN-CAT-CD   9(04) [12,16)
                ZonedDecimal.parse(line.substring(16, 22), 2));   // DIS-INT-RATE S9(04)V99  [16,22)
    }

    /** Parses {@code cardxref.txt} (CVACT03Y layout; the ASCII export omits the trailing FILLER). */
    public static List<CardXrefRecord> loadCardXrefs(Path file) {
        return lines(file).stream().map(AsciiFileLoader::parseCardXref).toList();
    }

    static CardXrefRecord parseCardXref(String line) {
        return new CardXrefRecord(
                line.substring(0, 16),                            // XREF-CARD-NUM X(16) [0,16)
                Long.parseLong(line.substring(16, 25)),           // XREF-CUST-ID  9(09) [16,25)
                Long.parseLong(line.substring(25, 36)));          // XREF-ACCT-ID  9(11) [25,36)
    }

    /** Parses {@code acctdata.txt} (CVACT01Y layout, RECLN 300). */
    public static List<AccountRecord> loadAccounts(Path file) {
        return lines(file).stream().map(AsciiFileLoader::parseAccount).toList();
    }

    static AccountRecord parseAccount(String line) {
        return new AccountRecord(
                Long.parseLong(line.substring(0, 11)),            // ACCT-ID                9(11)     [0,11)
                line.substring(11, 12),                           // ACCT-ACTIVE-STATUS     X(01)     [11,12)
                ZonedDecimal.parse(line.substring(12, 24), 2),    // ACCT-CURR-BAL          S9(10)V99 [12,24)
                ZonedDecimal.parse(line.substring(24, 36), 2),    // ACCT-CREDIT-LIMIT      S9(10)V99 [24,36)
                ZonedDecimal.parse(line.substring(36, 48), 2),    // ACCT-CASH-CREDIT-LIMIT S9(10)V99 [36,48)
                LocalDate.parse(line.substring(48, 58)),          // ACCT-OPEN-DATE         X(10)     [48,58)
                LocalDate.parse(line.substring(58, 68)),          // ACCT-EXPIRAION-DATE    X(10)     [58,68)
                LocalDate.parse(line.substring(68, 78)),          // ACCT-REISSUE-DATE      X(10)     [68,78)
                ZonedDecimal.parse(line.substring(78, 90), 2),    // ACCT-CURR-CYC-CREDIT   S9(10)V99 [78,90)
                ZonedDecimal.parse(line.substring(90, 102), 2),   // ACCT-CURR-CYC-DEBIT    S9(10)V99 [90,102)
                line.substring(102, 112).trim(),                  // ACCT-ADDR-ZIP          X(10)     [102,112)
                line.substring(112, 122).trim());                 // ACCT-GROUP-ID          X(10)     [112,122)
    }

    private static List<String> lines(Path file) {
        try {
            return Files.readAllLines(file).stream().filter(l -> !l.isBlank()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading " + file, e);
        }
    }
}
