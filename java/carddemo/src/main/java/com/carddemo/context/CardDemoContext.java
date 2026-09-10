package com.carddemo.context;

import java.io.Serializable;

/**
 * Java counterpart of the {@code CARDDEMO-COMMAREA} (copybook {@code COCOM01Y}) that CICS programs
 * pass to each other through {@code DFHCOMMAREA}.
 *
 * <p>The online layer carries one instance per user session instead of a byte area, so the field
 * lengths of the copybook are documented rather than enforced.
 */
public class CardDemoContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /** {@code CDEMO-FROM-TRANID PIC X(04)} */
    private String fromTranId;
    /** {@code CDEMO-FROM-PROGRAM PIC X(08)} */
    private String fromProgram;
    /** {@code CDEMO-TO-TRANID PIC X(04)} */
    private String toTranId;
    /** {@code CDEMO-TO-PROGRAM PIC X(08)} */
    private String toProgram;
    /** {@code CDEMO-USER-ID PIC X(08)} */
    private String userId;
    /** {@code CDEMO-USER-TYPE PIC X(01)} */
    private UserType userType;
    /** {@code CDEMO-PGM-CONTEXT PIC 9(01)} */
    private ProgramContext programContext = ProgramContext.ENTER;

    /** {@code CDEMO-CUST-ID PIC 9(09)} */
    private Long customerId;
    /** {@code CDEMO-CUST-FNAME PIC X(25)} */
    private String customerFirstName;
    /** {@code CDEMO-CUST-MNAME PIC X(25)} */
    private String customerMiddleName;
    /** {@code CDEMO-CUST-LNAME PIC X(25)} */
    private String customerLastName;

    /** {@code CDEMO-ACCT-ID PIC 9(11)} */
    private Long accountId;
    /** {@code CDEMO-ACCT-STATUS PIC X(01)} */
    private String accountStatus;

    /** {@code CDEMO-CARD-NUM PIC 9(16)} */
    private String cardNumber;

    /** {@code CDEMO-LAST-MAP PIC X(7)} */
    private String lastMap;
    /** {@code CDEMO-LAST-MAPSET PIC X(7)} */
    private String lastMapset;

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean isSignedOn() {
        return userId != null && !userId.isBlank();
    }

    public String getFromTranId() {
        return fromTranId;
    }

    public void setFromTranId(String fromTranId) {
        this.fromTranId = fromTranId;
    }

    public String getFromProgram() {
        return fromProgram;
    }

    public void setFromProgram(String fromProgram) {
        this.fromProgram = fromProgram;
    }

    public String getToTranId() {
        return toTranId;
    }

    public void setToTranId(String toTranId) {
        this.toTranId = toTranId;
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

    public ProgramContext getProgramContext() {
        return programContext;
    }

    public void setProgramContext(ProgramContext programContext) {
        this.programContext = programContext;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
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
}
