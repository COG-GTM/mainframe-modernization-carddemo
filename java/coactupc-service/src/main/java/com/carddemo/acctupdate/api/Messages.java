package com.carddemo.acctupdate.api;

public final class Messages {
    private Messages() {}
    public static final String ENTER_ACCOUNT = "Enter or update id of account to update";
    public static final String DETAILS_SHOWN = "Details of selected account shown above";
    public static final String UPDATE_PRESENTED = "Update account details presented above.";
    public static final String CHANGES_VALIDATED = "Changes validated.Press F5 to save";
    public static final String COMMITTED = "Changes committed to database";
    public static final String UNSUCCESSFUL = "Changes unsuccessful. Please try again";
    public static final String ACCOUNT_NOT_PROVIDED = "Account number not provided";
    public static final String ACCOUNT_INVALID = "Account number must be a non zero 11 digit number";
    public static final String NO_CHANGE = "No change detected with respect to values fetched.";
    public static final String XREF_NOT_FOUND = "Did not find this account in account card xref file";
    public static final String ACCOUNT_NOT_FOUND = "Did not find this account in account master file";
    public static final String CUSTOMER_NOT_FOUND = "Did not find associated customer in master file";
    public static final String LOCK_ACCOUNT = "Could not lock account record for update";
    public static final String LOCK_CUSTOMER = "Could not lock customer record for update";
    public static final String DATA_CHANGED = "Record changed by some one else. Please review";
    public static final String UPDATE_FAILED = "Update of record failed";
    public static final String INVALID_ZIP = "Invalid zip code for state";
}
