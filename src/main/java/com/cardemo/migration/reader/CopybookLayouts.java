package com.cardemo.migration.reader;

import com.cardemo.migration.model.CopybookField;
import com.cardemo.migration.model.CopybookLayout;

import java.util.List;
import java.util.Map;

/**
 * Defines all copybook layouts for the 12 CardDemo VSAM files.
 * Byte offsets are computed from the COBOL PIC clauses in the .cpy files.
 */
public final class CopybookLayouts {

    private CopybookLayouts() {
    }

    /**
     * CVACT01Y - Account master record (300 bytes).
     * Fields: ACCT-ID PIC 9(11), ACCT-ACTIVE-STATUS PIC X(01),
     * ACCT-CURR-BAL PIC S9(10)V99, ACCT-CREDIT-LIMIT PIC S9(10)V99,
     * ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99, ACCT-OPEN-DATE PIC X(10),
     * ACCT-EXPIRAION-DATE PIC X(10), ACCT-REISSUE-DATE PIC X(10),
     * ACCT-CURR-CYC-CREDIT PIC S9(10)V99, ACCT-CURR-CYC-DEBIT PIC S9(10)V99,
     * ACCT-ADDR-ZIP PIC X(10), ACCT-GROUP-ID PIC X(10), FILLER PIC X(178).
     */
    public static final CopybookLayout CVACT01Y = new CopybookLayout("CVACT01Y", 300, List.of(
            CopybookField.numericDisplay("ACCT-ID", 0, 11, "acct_id"),
            CopybookField.alphanumeric("ACCT-ACTIVE-STATUS", 11, 1, "active_status"),
            CopybookField.signedNumericDisplay("ACCT-CURR-BAL", 12, 12, 2, "curr_bal"),
            CopybookField.signedNumericDisplay("ACCT-CREDIT-LIMIT", 24, 12, 2, "credit_limit"),
            CopybookField.signedNumericDisplay("ACCT-CASH-CREDIT-LIMIT", 36, 12, 2, "cash_credit_limit"),
            CopybookField.alphanumeric("ACCT-OPEN-DATE", 48, 10, "open_date"),
            CopybookField.alphanumeric("ACCT-EXPIRAION-DATE", 58, 10, "expiration_date"),
            CopybookField.alphanumeric("ACCT-REISSUE-DATE", 68, 10, "reissue_date"),
            CopybookField.signedNumericDisplay("ACCT-CURR-CYC-CREDIT", 78, 12, 2, "curr_cyc_credit"),
            CopybookField.signedNumericDisplay("ACCT-CURR-CYC-DEBIT", 90, 12, 2, "curr_cyc_debit"),
            CopybookField.alphanumeric("ACCT-ADDR-ZIP", 102, 10, "addr_zip"),
            CopybookField.alphanumeric("ACCT-GROUP-ID", 112, 10, "group_id"),
            CopybookField.filler(122, 178)
    ));

    /**
     * CVACT02Y - Card data record (150 bytes).
     * Fields: CARD-NUM PIC X(16), CARD-ACCT-ID PIC 9(11),
     * CARD-CVV-CD PIC 9(03), CARD-EMBOSSED-NAME PIC X(50),
     * CARD-EXPIRAION-DATE PIC X(10), CARD-ACTIVE-STATUS PIC X(01), FILLER PIC X(59).
     */
    public static final CopybookLayout CVACT02Y = new CopybookLayout("CVACT02Y", 150, List.of(
            CopybookField.alphanumeric("CARD-NUM", 0, 16, "card_num"),
            CopybookField.numericDisplay("CARD-ACCT-ID", 16, 11, "acct_id"),
            CopybookField.numericDisplay("CARD-CVV-CD", 27, 3, "cvv_cd"),
            CopybookField.alphanumeric("CARD-EMBOSSED-NAME", 30, 50, "embossed_name"),
            CopybookField.alphanumeric("CARD-EXPIRAION-DATE", 80, 10, "expiration_date"),
            CopybookField.alphanumeric("CARD-ACTIVE-STATUS", 90, 1, "active_status"),
            CopybookField.filler(91, 59)
    ));

    /**
     * CVACT03Y - Card cross-reference record (50 bytes).
     * Fields: XREF-CARD-NUM PIC X(16), XREF-CUST-ID PIC 9(09),
     * XREF-ACCT-ID PIC 9(11), FILLER PIC X(14).
     */
    public static final CopybookLayout CVACT03Y = new CopybookLayout("CVACT03Y", 50, List.of(
            CopybookField.alphanumeric("XREF-CARD-NUM", 0, 16, "card_num"),
            CopybookField.numericDisplay("XREF-CUST-ID", 16, 9, "cust_id"),
            CopybookField.numericDisplay("XREF-ACCT-ID", 25, 11, "acct_id"),
            CopybookField.filler(36, 14)
    ));

    /**
     * CVCUS01Y - Customer data record (500 bytes).
     * Fields: CUST-ID PIC 9(09), CUST-FIRST-NAME PIC X(25), CUST-MIDDLE-NAME PIC X(25),
     * CUST-LAST-NAME PIC X(25), CUST-ADDR-LINE-1 PIC X(50), CUST-ADDR-LINE-2 PIC X(50),
     * CUST-ADDR-LINE-3 PIC X(50), CUST-ADDR-STATE-CD PIC X(02), CUST-ADDR-COUNTRY-CD PIC X(03),
     * CUST-ADDR-ZIP PIC X(10), CUST-PHONE-NUM-1 PIC X(15), CUST-PHONE-NUM-2 PIC X(15),
     * CUST-SSN PIC 9(09), CUST-GOVT-ISSUED-ID PIC X(20), CUST-DOB-YYYY-MM-DD PIC X(10),
     * CUST-EFT-ACCOUNT-ID PIC X(10), CUST-PRI-CARD-HOLDER-IND PIC X(01),
     * CUST-FICO-CREDIT-SCORE PIC 9(03), FILLER PIC X(168).
     */
    public static final CopybookLayout CVCUS01Y = new CopybookLayout("CVCUS01Y", 500, List.of(
            CopybookField.numericDisplay("CUST-ID", 0, 9, "cust_id"),
            CopybookField.alphanumeric("CUST-FIRST-NAME", 9, 25, "first_name"),
            CopybookField.alphanumeric("CUST-MIDDLE-NAME", 34, 25, "middle_name"),
            CopybookField.alphanumeric("CUST-LAST-NAME", 59, 25, "last_name"),
            CopybookField.alphanumeric("CUST-ADDR-LINE-1", 84, 50, "addr_line_1"),
            CopybookField.alphanumeric("CUST-ADDR-LINE-2", 134, 50, "addr_line_2"),
            CopybookField.alphanumeric("CUST-ADDR-LINE-3", 184, 50, "addr_line_3"),
            CopybookField.alphanumeric("CUST-ADDR-STATE-CD", 234, 2, "addr_state_cd"),
            CopybookField.alphanumeric("CUST-ADDR-COUNTRY-CD", 236, 3, "addr_country_cd"),
            CopybookField.alphanumeric("CUST-ADDR-ZIP", 239, 10, "addr_zip"),
            CopybookField.alphanumeric("CUST-PHONE-NUM-1", 249, 15, "phone_num_1"),
            CopybookField.alphanumeric("CUST-PHONE-NUM-2", 264, 15, "phone_num_2"),
            CopybookField.numericDisplay("CUST-SSN", 279, 9, "ssn"),
            CopybookField.alphanumeric("CUST-GOVT-ISSUED-ID", 288, 20, "govt_issued_id"),
            CopybookField.alphanumeric("CUST-DOB-YYYY-MM-DD", 308, 10, "dob"),
            CopybookField.alphanumeric("CUST-EFT-ACCOUNT-ID", 318, 10, "eft_account_id"),
            CopybookField.alphanumeric("CUST-PRI-CARD-HOLDER-IND", 328, 1, "pri_card_holder_ind"),
            CopybookField.numericDisplay("CUST-FICO-CREDIT-SCORE", 329, 3, "fico_credit_score"),
            CopybookField.filler(332, 168)
    ));

    /**
     * CVTRA05Y - Transaction record (350 bytes).
     * Fields: TRAN-ID PIC X(16), TRAN-TYPE-CD PIC X(02), TRAN-CAT-CD PIC 9(04),
     * TRAN-SOURCE PIC X(10), TRAN-DESC PIC X(100), TRAN-AMT PIC S9(09)V99,
     * TRAN-MERCHANT-ID PIC 9(09), TRAN-MERCHANT-NAME PIC X(50),
     * TRAN-MERCHANT-CITY PIC X(50), TRAN-MERCHANT-ZIP PIC X(10),
     * TRAN-CARD-NUM PIC X(16), TRAN-ORIG-TS PIC X(26), TRAN-PROC-TS PIC X(26),
     * FILLER PIC X(20).
     */
    public static final CopybookLayout CVTRA05Y = new CopybookLayout("CVTRA05Y", 350, List.of(
            CopybookField.alphanumeric("TRAN-ID", 0, 16, "tran_id"),
            CopybookField.alphanumeric("TRAN-TYPE-CD", 16, 2, "type_cd"),
            CopybookField.numericDisplay("TRAN-CAT-CD", 18, 4, "cat_cd"),
            CopybookField.alphanumeric("TRAN-SOURCE", 22, 10, "source"),
            CopybookField.alphanumeric("TRAN-DESC", 32, 100, "description"),
            CopybookField.signedNumericDisplay("TRAN-AMT", 132, 11, 2, "amount"),
            CopybookField.numericDisplay("TRAN-MERCHANT-ID", 143, 9, "merchant_id"),
            CopybookField.alphanumeric("TRAN-MERCHANT-NAME", 152, 50, "merchant_name"),
            CopybookField.alphanumeric("TRAN-MERCHANT-CITY", 202, 50, "merchant_city"),
            CopybookField.alphanumeric("TRAN-MERCHANT-ZIP", 252, 10, "merchant_zip"),
            CopybookField.alphanumeric("TRAN-CARD-NUM", 262, 16, "card_num"),
            CopybookField.alphanumeric("TRAN-ORIG-TS", 278, 26, "orig_ts"),
            CopybookField.alphanumeric("TRAN-PROC-TS", 304, 26, "proc_ts"),
            CopybookField.filler(330, 20)
    ));

    /**
     * CVTRA06Y - Daily transaction record (350 bytes). Same layout as CVTRA05Y
     * but with DALYTRAN- prefix.
     */
    public static final CopybookLayout CVTRA06Y = new CopybookLayout("CVTRA06Y", 350, List.of(
            CopybookField.alphanumeric("DALYTRAN-ID", 0, 16, "tran_id"),
            CopybookField.alphanumeric("DALYTRAN-TYPE-CD", 16, 2, "type_cd"),
            CopybookField.numericDisplay("DALYTRAN-CAT-CD", 18, 4, "cat_cd"),
            CopybookField.alphanumeric("DALYTRAN-SOURCE", 22, 10, "source"),
            CopybookField.alphanumeric("DALYTRAN-DESC", 32, 100, "description"),
            CopybookField.signedNumericDisplay("DALYTRAN-AMT", 132, 11, 2, "amount"),
            CopybookField.numericDisplay("DALYTRAN-MERCHANT-ID", 143, 9, "merchant_id"),
            CopybookField.alphanumeric("DALYTRAN-MERCHANT-NAME", 152, 50, "merchant_name"),
            CopybookField.alphanumeric("DALYTRAN-MERCHANT-CITY", 202, 50, "merchant_city"),
            CopybookField.alphanumeric("DALYTRAN-MERCHANT-ZIP", 252, 10, "merchant_zip"),
            CopybookField.alphanumeric("DALYTRAN-CARD-NUM", 262, 16, "card_num"),
            CopybookField.alphanumeric("DALYTRAN-ORIG-TS", 278, 26, "orig_ts"),
            CopybookField.alphanumeric("DALYTRAN-PROC-TS", 304, 26, "proc_ts"),
            CopybookField.filler(330, 20)
    ));

    /**
     * CVTRA01Y - Transaction category balance record (50 bytes).
     * Fields: TRANCAT-ACCT-ID PIC 9(11), TRANCAT-TYPE-CD PIC X(02),
     * TRANCAT-CD PIC 9(04), TRAN-CAT-BAL PIC S9(09)V99, FILLER PIC X(22).
     */
    public static final CopybookLayout CVTRA01Y = new CopybookLayout("CVTRA01Y", 50, List.of(
            CopybookField.numericDisplay("TRANCAT-ACCT-ID", 0, 11, "acct_id"),
            CopybookField.alphanumeric("TRANCAT-TYPE-CD", 11, 2, "type_cd"),
            CopybookField.numericDisplay("TRANCAT-CD", 13, 4, "cat_cd"),
            CopybookField.signedNumericDisplay("TRAN-CAT-BAL", 17, 11, 2, "balance"),
            CopybookField.filler(28, 22)
    ));

    /**
     * CVTRA02Y - Disclosure group record (50 bytes).
     * Fields: DIS-ACCT-GROUP-ID PIC X(10), DIS-TRAN-TYPE-CD PIC X(02),
     * DIS-TRAN-CAT-CD PIC 9(04), DIS-INT-RATE PIC S9(04)V99, FILLER PIC X(28).
     */
    public static final CopybookLayout CVTRA02Y = new CopybookLayout("CVTRA02Y", 50, List.of(
            CopybookField.alphanumeric("DIS-ACCT-GROUP-ID", 0, 10, "acct_group_id"),
            CopybookField.alphanumeric("DIS-TRAN-TYPE-CD", 10, 2, "tran_type_cd"),
            CopybookField.numericDisplay("DIS-TRAN-CAT-CD", 12, 4, "tran_cat_cd"),
            CopybookField.signedNumericDisplay("DIS-INT-RATE", 16, 6, 2, "int_rate"),
            CopybookField.filler(22, 28)
    ));

    /**
     * CSUSR01Y - User security record (80 bytes).
     * Fields: SEC-USR-ID PIC X(08), SEC-USR-FNAME PIC X(20),
     * SEC-USR-LNAME PIC X(20), SEC-USR-PWD PIC X(08),
     * SEC-USR-TYPE PIC X(01), SEC-USR-FILLER PIC X(23).
     */
    public static final CopybookLayout CSUSR01Y = new CopybookLayout("CSUSR01Y", 80, List.of(
            CopybookField.alphanumeric("SEC-USR-ID", 0, 8, "user_id"),
            CopybookField.alphanumeric("SEC-USR-FNAME", 8, 20, "first_name"),
            CopybookField.alphanumeric("SEC-USR-LNAME", 28, 20, "last_name"),
            CopybookField.alphanumeric("SEC-USR-PWD", 48, 8, "password"),
            CopybookField.alphanumeric("SEC-USR-TYPE", 56, 1, "user_type"),
            CopybookField.filler(57, 23)
    ));

    /** Lookup map from copybook name to layout. */
    private static final Map<String, CopybookLayout> LAYOUTS = Map.of(
            "CVACT01Y", CVACT01Y,
            "CVACT02Y", CVACT02Y,
            "CVACT03Y", CVACT03Y,
            "CVCUS01Y", CVCUS01Y,
            "CVTRA05Y", CVTRA05Y,
            "CVTRA06Y", CVTRA06Y,
            "CVTRA01Y", CVTRA01Y,
            "CVTRA02Y", CVTRA02Y,
            "CSUSR01Y", CSUSR01Y
    );

    /**
     * Returns the copybook layout for the given name.
     *
     * @param copybookName the copybook name (e.g. CVACT01Y)
     * @return the layout, or null if not defined
     */
    public static CopybookLayout getLayout(String copybookName) {
        return LAYOUTS.get(copybookName);
    }
}
