package com.carddemo.dto;

import com.carddemo.enums.ProgramContext;
import java.io.Serializable;

/**
 * Session-scoped DTO replacing COBOL COMMAREA (COCOM01Y.cpy).
 * Stores user session state between requests, analogous to CICS COMMAREA passing.
 */
public class SessionContext implements Serializable {

    private String fromTranId;
    private String fromProgram;
    private String toTranId;
    private String toProgram;
    private String userId;
    private String userType;
    private ProgramContext pgmContext;
    private Long custId;
    private String custFirstName;
    private String custMiddleName;
    private String custLastName;
    private Long acctId;
    private String acctStatus;
    private String cardNum;
    private String lastMap;
    private String lastMapSet;

    public SessionContext() {
        this.pgmContext = ProgramContext.ENTER;
    }

    public String getFromTranId() { return fromTranId; }
    public void setFromTranId(String fromTranId) { this.fromTranId = fromTranId; }
    public String getFromProgram() { return fromProgram; }
    public void setFromProgram(String fromProgram) { this.fromProgram = fromProgram; }
    public String getToTranId() { return toTranId; }
    public void setToTranId(String toTranId) { this.toTranId = toTranId; }
    public String getToProgram() { return toProgram; }
    public void setToProgram(String toProgram) { this.toProgram = toProgram; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public ProgramContext getPgmContext() { return pgmContext; }
    public void setPgmContext(ProgramContext pgmContext) { this.pgmContext = pgmContext; }
    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }
    public String getCustFirstName() { return custFirstName; }
    public void setCustFirstName(String custFirstName) { this.custFirstName = custFirstName; }
    public String getCustMiddleName() { return custMiddleName; }
    public void setCustMiddleName(String custMiddleName) { this.custMiddleName = custMiddleName; }
    public String getCustLastName() { return custLastName; }
    public void setCustLastName(String custLastName) { this.custLastName = custLastName; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public String getAcctStatus() { return acctStatus; }
    public void setAcctStatus(String acctStatus) { this.acctStatus = acctStatus; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public String getLastMap() { return lastMap; }
    public void setLastMap(String lastMap) { this.lastMap = lastMap; }
    public String getLastMapSet() { return lastMapSet; }
    public void setLastMapSet(String lastMapSet) { this.lastMapSet = lastMapSet; }
}
