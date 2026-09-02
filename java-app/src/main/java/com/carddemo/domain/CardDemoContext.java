package com.carddemo.domain;

/**
 * CARDDEMO-COMMAREA, copybook COCOM01Y.
 *
 * <p>Carries pseudo-conversational state between the online programs: who is signed on, where the
 * navigation came from and is headed, and the account / card / customer currently in context.
 */
public class CardDemoContext {

    private String fromTransactionId;
    private String fromProgram;
    private String toTransactionId;
    private String toProgram;
    private String userId;
    private char userType;
    private int programContext;
    private String customerId;
    private String customerFirstName;
    private String customerMiddleName;
    private String customerLastName;
    private String accountId;
    private char accountStatus;
    private String cardNumber;
    private String lastMap;
    private String lastMapset;

    public boolean isAdmin() {
        return userType == User.TYPE_ADMIN;
    }

    public boolean isRegularUser() {
        return userType == User.TYPE_REGULAR;
    }

    /** CDEMO-PGM-ENTER: first entry into the program. */
    public boolean isFirstEntry() {
        return programContext == 0;
    }

    /** CDEMO-PGM-REENTER: re-entry after a pseudo-conversational return. */
    public boolean isReentry() {
        return programContext == 1;
    }

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

    public char getUserType() {
        return userType;
    }

    public void setUserType(char userType) {
        this.userType = userType;
    }

    public int getProgramContext() {
        return programContext;
    }

    public void setProgramContext(int programContext) {
        this.programContext = programContext;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public char getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(char accountStatus) {
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
}
