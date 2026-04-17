package com.carddemo.shared.dto;

import com.carddemo.shared.enums.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps to CDEMO-GENERAL-INFO in COCOM01Y.cpy.
 *
 * <pre>
 * 05 CDEMO-GENERAL-INFO.
 *   10 CDEMO-FROM-TRANID         PIC X(04).
 *   10 CDEMO-FROM-PROGRAM        PIC X(08).
 *   10 CDEMO-TO-TRANID           PIC X(04).
 *   10 CDEMO-TO-PROGRAM          PIC X(08).
 *   10 CDEMO-USER-ID             PIC X(08).
 *   10 CDEMO-USER-TYPE           PIC X(01).
 *   10 CDEMO-PGM-CONTEXT         PIC 9(01).
 * </pre>
 */
public class GeneralInfo {

    @JsonProperty("fromTranId")
    private String fromTranId;

    @JsonProperty("fromProgram")
    private String fromProgram;

    @JsonProperty("toTranId")
    private String toTranId;

    @JsonProperty("toProgram")
    private String toProgram;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userType")
    private UserType userType;

    @JsonProperty("pgmContext")
    private int pgmContext;

    public GeneralInfo() {
    }

    public GeneralInfo(String fromTranId, String fromProgram, String toTranId,
                       String toProgram, String userId, UserType userType, int pgmContext) {
        this.fromTranId = fromTranId;
        this.fromProgram = fromProgram;
        this.toTranId = toTranId;
        this.toProgram = toProgram;
        this.userId = userId;
        this.userType = userType;
        this.pgmContext = pgmContext;
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

    public int getPgmContext() {
        return pgmContext;
    }

    public void setPgmContext(int pgmContext) {
        this.pgmContext = pgmContext;
    }

    @JsonIgnore
    public boolean isEnter() {
        return pgmContext == 0;
    }

    @JsonIgnore
    public boolean isReenter() {
        return pgmContext == 1;
    }
}
