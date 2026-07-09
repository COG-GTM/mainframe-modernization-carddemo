package com.carddemo.service.billpay;

/**
 * Verbatim operator messages from {@code COBIL00C} ({@code ERRMSGO} of map {@code COBIL0A}),
 * reused by the REST bill-payment flow so the migrated logic returns the exact wording a
 * 3270 operator saw.
 */
public final class BillPayMessages {

    /** {@code WHEN ACTIDINI = SPACES OR LOW-VALUES} branch. */
    public static final String EMPTY_ACCT_ID = "Acct ID can NOT be empty...";

    /** {@code READ-ACCTDAT-FILE} / {@code READ-CXACAIX-FILE} {@code DFHRESP(NOTFND)} branch. */
    public static final String ACCT_NOT_FOUND = "Account ID NOT found...";

    /** {@code EVALUATE CONFIRMI ... WHEN OTHER} branch. */
    public static final String INVALID_CONFIRM = "Invalid value. Valid values are (Y/N)...";

    /** {@code IF ACCT-CURR-BAL <= ZEROS} branch. */
    public static final String NOTHING_TO_PAY = "You have nothing to pay...";

    /** {@code CONF-PAY-NO} branch — a payment was not yet confirmed. */
    public static final String CONFIRM_PAYMENT = "Confirm to make a bill payment...";

    private BillPayMessages() {
    }

    /**
     * Success message built by the COBOL {@code STRING 'Payment successful. ' ' Your
     * Transaction ID is ' TRAN-ID '.'} statement (note the two spaces between the sentences).
     */
    public static String paymentSuccessful(String transactionId) {
        return "Payment successful.  Your Transaction ID is " + transactionId + ".";
    }
}
