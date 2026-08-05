package com.cog.carddemo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The {@code ACCOUNT-RECORD} layout of copybook {@code CVACT01Y} (RECLN 300).
 *
 * <pre>
 *   ACCT-ID                PIC 9(11)      offset   0
 *   ACCT-ACTIVE-STATUS     PIC X(01)      offset  11
 *   ACCT-CURR-BAL          PIC S9(10)V99  offset  12
 *   ACCT-CREDIT-LIMIT      PIC S9(10)V99  offset  24
 *   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99  offset  36
 *   ACCT-OPEN-DATE         PIC X(10)      offset  48
 *   ACCT-EXPIRAION-DATE    PIC X(10)      offset  58
 *   ACCT-REISSUE-DATE      PIC X(10)      offset  68
 *   ACCT-CURR-CYC-CREDIT   PIC S9(10)V99  offset  78
 *   ACCT-CURR-CYC-DEBIT    PIC S9(10)V99  offset  90
 *   ACCT-ADDR-ZIP          PIC X(10)      offset 102
 *   ACCT-GROUP-ID          PIC X(10)      offset 112
 *   FILLER                 PIC X(178)     offset 122
 * </pre>
 */
public record AccountRecord(
        String acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String addrZip,
        String groupId,
        String filler) {

    /** Record length of the account master file. */
    public static final int LENGTH = 300;

    /** Digits of the {@code S9(10)V99} amount fields. */
    private static final int AMOUNT_PRECISION = 12;

    /** Implied decimal positions of the {@code S9(10)V99} amount fields. */
    private static final int AMOUNT_SCALE = 2;

    public AccountRecord {
        Objects.requireNonNull(acctId, "acctId");
        Objects.requireNonNull(activeStatus, "activeStatus");
        Objects.requireNonNull(currBal, "currBal");
        Objects.requireNonNull(creditLimit, "creditLimit");
        Objects.requireNonNull(cashCreditLimit, "cashCreditLimit");
        Objects.requireNonNull(openDate, "openDate");
        Objects.requireNonNull(expirationDate, "expirationDate");
        Objects.requireNonNull(reissueDate, "reissueDate");
        Objects.requireNonNull(currCycCredit, "currCycCredit");
        Objects.requireNonNull(currCycDebit, "currCycDebit");
        Objects.requireNonNull(addrZip, "addrZip");
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(filler, "filler");
    }

    /** Parse one fixed length account record. */
    public static AccountRecord parse(String raw) {
        Objects.requireNonNull(raw, "raw");
        if (raw.length() != LENGTH) {
            throw new IllegalArgumentException(
                    "account record must be " + LENGTH + " characters, got " + raw.length());
        }
        return new AccountRecord(
                raw.substring(0, 11),
                raw.substring(11, 12),
                amount(raw, 12),
                amount(raw, 24),
                amount(raw, 36),
                raw.substring(48, 58),
                raw.substring(58, 68),
                raw.substring(68, 78),
                amount(raw, 78),
                amount(raw, 90),
                raw.substring(102, 112),
                raw.substring(112, 122),
                raw.substring(122, LENGTH));
    }

    /**
     * Serialize back to the 300 character layout. Amounts are written with the
     * ASCII sign convention, so a record read from EBCDIC exported data comes
     * back with the positive overpunch normalized to a plain digit, exactly as
     * the COBOL runtime rewrites it.
     */
    public String toRawRecord() {
        return acctId
                + activeStatus
                + zoned(currBal)
                + zoned(creditLimit)
                + zoned(cashCreditLimit)
                + openDate
                + expirationDate
                + reissueDate
                + zoned(currCycCredit)
                + zoned(currCycDebit)
                + addrZip
                + groupId
                + filler;
    }

    /** Render an amount field the way {@code DISPLAY} renders it. */
    public static String display(BigDecimal amount) {
        return CobolDecimal.formatDisplay(amount, AMOUNT_PRECISION, AMOUNT_SCALE);
    }

    private static BigDecimal amount(String raw, int offset) {
        return CobolDecimal.parseZoned(raw.substring(offset, offset + AMOUNT_PRECISION), AMOUNT_SCALE);
    }

    private static String zoned(BigDecimal amount) {
        return CobolDecimal.formatZoned(amount, AMOUNT_PRECISION, AMOUNT_SCALE);
    }
}
