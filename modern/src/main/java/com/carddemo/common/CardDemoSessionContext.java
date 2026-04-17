package com.carddemo.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Session context DTO replacing the COBOL COMMAREA ({@code COCOM01Y.cpy}).
 *
 * <p>The COMMAREA is the inter-program communication structure used by
 * CICS transactions to pass state between programs. This class provides
 * a typed Java equivalent, preserving all fields from the original
 * {@code CARDDEMO-COMMAREA} copybook layout.</p>
 *
 * <h3>Original COBOL Layout</h3>
 * <pre>
 * 01 CARDDEMO-COMMAREA.
 *    05 CDEMO-GENERAL-INFO.
 *       10 CDEMO-FROM-TRANID      PIC X(04).
 *       10 CDEMO-FROM-PROGRAM     PIC X(08).
 *       10 CDEMO-TO-TRANID        PIC X(04).
 *       10 CDEMO-TO-PROGRAM       PIC X(08).
 *       10 CDEMO-USER-ID          PIC X(08).
 *       10 CDEMO-USER-TYPE        PIC X(01).
 *       10 CDEMO-PGM-CONTEXT      PIC 9(01).
 *    05 CDEMO-CUSTOMER-INFO.
 *       10 CDEMO-CUST-ID          PIC 9(09).
 *       10 CDEMO-CUST-FNAME       PIC X(25).
 *       10 CDEMO-CUST-MNAME       PIC X(25).
 *       10 CDEMO-CUST-LNAME       PIC X(25).
 *    05 CDEMO-ACCOUNT-INFO.
 *       10 CDEMO-ACCT-ID          PIC 9(11).
 *       10 CDEMO-ACCT-STATUS      PIC X(01).
 *    05 CDEMO-CARD-INFO.
 *       10 CDEMO-CARD-NUM         PIC 9(16).
 *    05 CDEMO-MORE-INFO.
 *       10 CDEMO-LAST-MAP         PIC X(7).
 *       10 CDEMO-LAST-MAPSET      PIC X(7).
 * </pre>
 */
public class CardDemoSessionContext {

    // --- General Info (CDEMO-GENERAL-INFO) ---

    /** Source transaction ID, e.g. "CC00". Maps to CDEMO-FROM-TRANID PIC X(04). */
    @Size(max = 4)
    private String fromTransactionId;

    /** Source program name, e.g. "COSGN00C". Maps to CDEMO-FROM-PROGRAM PIC X(08). */
    @Size(max = 8)
    private String fromProgram;

    /** Target transaction ID. Maps to CDEMO-TO-TRANID PIC X(04). */
    @Size(max = 4)
    private String toTransactionId;

    /** Target program name. Maps to CDEMO-TO-PROGRAM PIC X(08). */
    @Size(max = 8)
    private String toProgram;

    /** Authenticated user ID. Maps to CDEMO-USER-ID PIC X(08). */
    @NotBlank
    @Size(max = 8)
    private String userId;

    /** User type: ADMIN or USER. Maps to CDEMO-USER-TYPE PIC X(01). */
    @NotNull
    private UserType userType;

    /**
     * Program context indicator. Maps to CDEMO-PGM-CONTEXT PIC 9(01).
     * <ul>
     *   <li>0 = Initial entry (CDEMO-PGM-ENTER)</li>
     *   <li>1 = Re-entry (CDEMO-PGM-REENTER)</li>
     * </ul>
     */
    @PositiveOrZero
    private int programContext;

    // --- Customer Info (CDEMO-CUSTOMER-INFO) ---

    /** Customer ID. Maps to CDEMO-CUST-ID PIC 9(09). */
    @PositiveOrZero
    private long customerId;

    /** Customer first name. Maps to CDEMO-CUST-FNAME PIC X(25). */
    @Size(max = 25)
    private String customerFirstName;

    /** Customer middle name. Maps to CDEMO-CUST-MNAME PIC X(25). */
    @Size(max = 25)
    private String customerMiddleName;

    /** Customer last name. Maps to CDEMO-CUST-LNAME PIC X(25). */
    @Size(max = 25)
    private String customerLastName;

    // --- Account Info (CDEMO-ACCOUNT-INFO) ---

    /** Account ID. Maps to CDEMO-ACCT-ID PIC 9(11). */
    @PositiveOrZero
    private long accountId;

    /** Account status code. Maps to CDEMO-ACCT-STATUS PIC X(01). */
    @Size(max = 1)
    private String accountStatus;

    // --- Card Info (CDEMO-CARD-INFO) ---

    /** Card number. Maps to CDEMO-CARD-NUM PIC 9(16). */
    @Size(max = 16)
    private String cardNumber;

    // --- More Info (CDEMO-MORE-INFO) ---

    /** Last BMS map name displayed. Maps to CDEMO-LAST-MAP PIC X(7). */
    @Size(max = 7)
    private String lastMap;

    /** Last BMS mapset name used. Maps to CDEMO-LAST-MAPSET PIC X(7). */
    @Size(max = 7)
    private String lastMapset;

    /** Default constructor. */
    public CardDemoSessionContext() {
    }

    // --- Getters and Setters ---

    public String getFromTransactionId() {
        return fromTransactionId;
    }

    public void setFromTransactionId(String fromTransactionId) {
        this.fromTransactionId = fromTransactionId;
    }

    public String getFromProgram() {
        return fromProgram;
    }

    public void setFromProgram(String fromProgram) {
        this.fromProgram = fromProgram;
    }

    public String getToTransactionId() {
        return toTransactionId;
    }

    public void setToTransactionId(String toTransactionId) {
        this.toTransactionId = toTransactionId;
    }

    public String getToProgram() {
        return toProgram;
    }

    public void setToProgram(String toProgram) {
        this.toProgram = toProgram;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public int getProgramContext() {
        return programContext;
    }

    public void setProgramContext(int programContext) {
        this.programContext = programContext;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerMiddleName() {
        return customerMiddleName;
    }

    public void setCustomerMiddleName(String customerMiddleName) {
        this.customerMiddleName = customerMiddleName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getLastMap() {
        return lastMap;
    }

    public void setLastMap(String lastMap) {
        this.lastMap = lastMap;
    }

    public String getLastMapset() {
        return lastMapset;
    }

    public void setLastMapset(String lastMapset) {
        this.lastMapset = lastMapset;
    }

    /**
     * Returns {@code true} if the program context indicates initial entry.
     * Equivalent to COBOL condition {@code CDEMO-PGM-ENTER} (value 0).
     */
    public boolean isInitialEntry() {
        return programContext == 0;
    }

    /**
     * Returns {@code true} if the program context indicates re-entry.
     * Equivalent to COBOL condition {@code CDEMO-PGM-REENTER} (value 1).
     */
    public boolean isReentry() {
        return programContext == 1;
    }

    @Override
    public String toString() {
        return "CardDemoSessionContext{" +
                "fromTransactionId='" + fromTransactionId + '\'' +
                ", fromProgram='" + fromProgram + '\'' +
                ", toTransactionId='" + toTransactionId + '\'' +
                ", toProgram='" + toProgram + '\'' +
                ", userId='" + userId + '\'' +
                ", userType=" + userType +
                ", programContext=" + programContext +
                ", customerId=" + customerId +
                ", accountId=" + accountId +
                ", cardNumber='" + cardNumber + '\'' +
                '}';
    }
}
