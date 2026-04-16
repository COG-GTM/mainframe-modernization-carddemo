package com.cardemo.gateway.model;

import com.cardemo.gateway.enums.ProgramContext;
import com.cardemo.gateway.enums.UserType;

/**
 * Modernized representation of the CARDDEMO-COMMAREA (COCOM01Y.cpy).
 * Replaces the flat byte buffer with a typed Java object.
 *
 * COBOL layout:
 *   CDEMO-FROM-TRANID    PIC X(04)
 *   CDEMO-FROM-PROGRAM   PIC X(08)
 *   CDEMO-TO-TRANID      PIC X(04)
 *   CDEMO-TO-PROGRAM     PIC X(08)
 *   CDEMO-USER-ID        PIC X(08)
 *   CDEMO-USER-TYPE      PIC X(01)  (A=admin, U=user)
 *   CDEMO-PGM-CONTEXT    PIC 9(01)  (0=enter, 1=reenter)
 */
public class SessionContext {

    private String fromTransactionId;
    private String fromProgram;
    private String toTransactionId;
    private String toProgram;
    private String userId;
    private UserType userType;
    private ProgramContext programContext;

    // Additional context from CDEMO-CUSTOMER-INFO, CDEMO-ACCOUNT-INFO, CDEMO-CARD-INFO
    private String customerId;
    private String accountId;
    private String cardNumber;
    private String lastMap;
    private String lastMapset;

    public SessionContext() {
        this.programContext = ProgramContext.ENTER;
    }

    public SessionContext(String userId, UserType userType) {
        this.userId = userId;
        this.userType = userType;
        this.programContext = ProgramContext.ENTER;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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
