package com.carddemo.etl.reader;

import com.carddemo.etl.codec.SignOverpunchDecoder;
import com.carddemo.etl.model.Account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Parses a single fixed-length Account record (300 characters) into an {@link Account}, using the
 * byte offsets defined by copybook {@code CVACT01Y}.
 *
 * <p>Offsets and lengths (1-based, inclusive) of {@code ACCOUNT-RECORD}:
 *
 * <pre>
 *   ACCT-ID                PIC 9(11)      pos 1..11    len 11
 *   ACCT-ACTIVE-STATUS     PIC X(01)      pos 12..12   len 1
 *   ACCT-CURR-BAL          PIC S9(10)V99  pos 13..24   len 12
 *   ACCT-CREDIT-LIMIT      PIC S9(10)V99  pos 25..36   len 12
 *   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99  pos 37..48   len 12
 *   ACCT-OPEN-DATE         PIC X(10)      pos 49..58   len 10
 *   ACCT-EXPIRAION-DATE    PIC X(10)      pos 59..68   len 10
 *   ACCT-REISSUE-DATE      PIC X(10)      pos 69..78   len 10
 *   ACCT-CURR-CYC-CREDIT   PIC S9(10)V99  pos 79..90   len 12
 *   ACCT-CURR-CYC-DEBIT    PIC S9(10)V99  pos 91..102  len 12
 *   ACCT-ADDR-ZIP          PIC X(10)      pos 103..112 len 10
 *   ACCT-GROUP-ID          PIC X(10)      pos 113..122 len 10
 *   FILLER                 PIC X(178)     pos 123..300 len 178
 * </pre>
 */
public final class AccountRecordParser {

    public static final int RECORD_LENGTH = 300;

    private static final int MONEY_SCALE = 2;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    // 0-based [start, end) offsets derived from the copybook.
    private static final int ACCT_ID_START = 0,            ACCT_ID_END = 11;
    private static final int STATUS_START = 11,            STATUS_END = 12;
    private static final int CURR_BAL_START = 12,          CURR_BAL_END = 24;
    private static final int CREDIT_LIMIT_START = 24,      CREDIT_LIMIT_END = 36;
    private static final int CASH_LIMIT_START = 36,        CASH_LIMIT_END = 48;
    private static final int OPEN_DATE_START = 48,         OPEN_DATE_END = 58;
    private static final int EXPIRATION_DATE_START = 58,   EXPIRATION_DATE_END = 68;
    private static final int REISSUE_DATE_START = 68,      REISSUE_DATE_END = 78;
    private static final int CYC_CREDIT_START = 78,        CYC_CREDIT_END = 90;
    private static final int CYC_DEBIT_START = 90,         CYC_DEBIT_END = 102;
    private static final int ADDR_ZIP_START = 102,         ADDR_ZIP_END = 112;
    private static final int GROUP_ID_START = 112,         GROUP_ID_END = 122;

    private AccountRecordParser() {
    }

    /**
     * Parses one decoded record into an {@link Account}.
     *
     * @param record the decoded record characters; must be at least {@link #RECORD_LENGTH} long
     * @throws RecordParseException if the record is too short or a field cannot be decoded
     */
    public static Account parse(String record) {
        if (record == null) {
            throw new RecordParseException("Record is null");
        }
        if (record.length() < RECORD_LENGTH) {
            throw new RecordParseException(
                    "Record length " + record.length() + " is shorter than expected " + RECORD_LENGTH);
        }

        long acctId = parseAccountId(field(record, ACCT_ID_START, ACCT_ID_END));
        String status = field(record, STATUS_START, STATUS_END).trim();
        BigDecimal currBal = money(record, CURR_BAL_START, CURR_BAL_END, "ACCT-CURR-BAL");
        BigDecimal creditLimit = money(record, CREDIT_LIMIT_START, CREDIT_LIMIT_END, "ACCT-CREDIT-LIMIT");
        BigDecimal cashLimit = money(record, CASH_LIMIT_START, CASH_LIMIT_END, "ACCT-CASH-CREDIT-LIMIT");
        LocalDate openDate = date(record, OPEN_DATE_START, OPEN_DATE_END, "ACCT-OPEN-DATE");
        LocalDate expirationDate = date(record, EXPIRATION_DATE_START, EXPIRATION_DATE_END, "ACCT-EXPIRAION-DATE");
        LocalDate reissueDate = date(record, REISSUE_DATE_START, REISSUE_DATE_END, "ACCT-REISSUE-DATE");
        BigDecimal cycCredit = money(record, CYC_CREDIT_START, CYC_CREDIT_END, "ACCT-CURR-CYC-CREDIT");
        BigDecimal cycDebit = money(record, CYC_DEBIT_START, CYC_DEBIT_END, "ACCT-CURR-CYC-DEBIT");
        String addrZip = trimToNull(field(record, ADDR_ZIP_START, ADDR_ZIP_END));
        String groupId = trimToNull(field(record, GROUP_ID_START, GROUP_ID_END));

        return new Account(acctId, status, currBal, creditLimit, cashLimit,
                openDate, expirationDate, reissueDate, cycCredit, cycDebit, addrZip, groupId);
    }

    private static String field(String record, int start, int end) {
        return record.substring(start, end);
    }

    private static long parseAccountId(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new RecordParseException("ACCT-ID is blank");
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            throw new RecordParseException("ACCT-ID is not numeric: \"" + raw + "\"", e);
        }
    }

    private static BigDecimal money(String record, int start, int end, String fieldName) {
        String raw = field(record, start, end);
        if (raw.isBlank()) {
            return new BigDecimal("0.00");
        }
        try {
            return SignOverpunchDecoder.decode(raw, MONEY_SCALE);
        } catch (NumberFormatException e) {
            throw new RecordParseException(
                    "Cannot decode " + fieldName + " from \"" + raw + "\"", e);
        }
    }

    private static LocalDate date(String record, int start, int end, String fieldName) {
        String raw = field(record, start, end).trim();
        if (raw.isEmpty() || raw.chars().allMatch(c -> c == '0')) {
            return null;
        }
        try {
            return LocalDate.parse(raw, ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new RecordParseException(
                    "Cannot parse " + fieldName + " as ISO date from \"" + raw + "\"", e);
        }
    }

    private static String trimToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
