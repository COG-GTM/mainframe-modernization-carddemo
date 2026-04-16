package com.carddemo.shared.constants;

/**
 * Error message constants used across the CardDemo application.
 * Messages are character-for-character identical to the original COBOL source.
 */
public final class CardDemoErrorMessages {

    private CardDemoErrorMessages() {
        // Utility class — prevent instantiation
    }

    // ---------------------------------------------------------------
    // Sign-on messages (COSGN00C)
    // ---------------------------------------------------------------
    public static final String SIGN_ON_UNSUCCESSFUL = "Sign On Is Unsuccessful";

    // ---------------------------------------------------------------
    // Transaction processing messages (CBTRN02C)
    // ---------------------------------------------------------------

    /** Reject code 100 */
    public static final String INVALID_CARD_NUMBER_FOUND = "INVALID CARD NUMBER FOUND";

    /** Reject code 101 */
    public static final String ACCOUNT_RECORD_NOT_FOUND = "ACCOUNT RECORD NOT FOUND";

    /** Reject code 102 */
    public static final String CARD_RECORD_NOT_FOUND = "CARD RECORD NOT FOUND";

    /** Reject code 103 */
    public static final String TRANSACTION_TYPE_INVALID = "TRANSACTION TYPE INVALID";

    // ---------------------------------------------------------------
    // Reject codes associated with transaction processing
    // ---------------------------------------------------------------
    public static final int REJECT_CODE_INVALID_CARD_NUMBER = 100;
    public static final int REJECT_CODE_ACCOUNT_NOT_FOUND = 101;
    public static final int REJECT_CODE_CARD_NOT_FOUND = 102;
    public static final int REJECT_CODE_TRANSACTION_TYPE_INVALID = 103;

    // ---------------------------------------------------------------
    // Date validation messages (CSUTLDPY)
    // ---------------------------------------------------------------
    public static final String DATE_YEAR_MUST_BE_SUPPLIED = "Year must be supplied.";
    public static final String DATE_YEAR_MUST_BE_4_DIGIT = "must be 4 digit number.";
    public static final String DATE_CENTURY_NOT_VALID = "Century is not valid.";
    public static final String DATE_MONTH_MUST_BE_SUPPLIED = "Month must be supplied.";
    public static final String DATE_MONTH_MUST_BE_1_TO_12 = "Month must be a number between 1 and 12.";
    public static final String DATE_DAY_MUST_BE_SUPPLIED = "Day must be supplied.";
    public static final String DATE_DAY_MUST_BE_1_TO_31 = "day must be a number between 1 and 31.";
    public static final String DATE_CANNOT_HAVE_31_DAYS = "Cannot have 31 days in this month.";
    public static final String DATE_CANNOT_HAVE_30_DAYS = "Cannot have 30 days in this month.";
    public static final String DATE_NOT_LEAP_YEAR_29_DAYS = "Not a leap year.Cannot have 29 days in this month.";
    public static final String DATE_CANNOT_BE_IN_FUTURE = "cannot be in the future";
}
