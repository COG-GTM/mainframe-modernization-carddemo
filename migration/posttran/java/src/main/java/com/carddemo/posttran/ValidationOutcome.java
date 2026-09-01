package com.carddemo.posttran;

/**
 * The outcome of {@code 1500-VALIDATE-TRAN} — CBTRN02C.cbl:370-422.
 *
 * <p>Mirrors {@code WS-VALIDATION-TRAILER}: a {@code PIC 9(04)} reason and a {@code PIC X(76)}
 * description, where zero means "post it". The descriptions are the exact literals the COBOL
 * moves, because they are written verbatim into the reject record.
 */
public record ValidationOutcome(int reason, String description) {

    public static final ValidationOutcome OK = new ValidationOutcome(0, "");

    /** CBTRN02C.cbl:385-386 */
    public static final ValidationOutcome INVALID_CARD =
            new ValidationOutcome(100, "INVALID CARD NUMBER FOUND");
    /** CBTRN02C.cbl:397-398 */
    public static final ValidationOutcome ACCOUNT_NOT_FOUND =
            new ValidationOutcome(101, "ACCOUNT RECORD NOT FOUND");
    /** CBTRN02C.cbl:410-411 */
    public static final ValidationOutcome OVERLIMIT =
            new ValidationOutcome(102, "OVERLIMIT TRANSACTION");
    /** CBTRN02C.cbl:417-418 */
    public static final ValidationOutcome EXPIRED =
            new ValidationOutcome(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");

    public boolean rejected() {
        return reason != 0;
    }
}
