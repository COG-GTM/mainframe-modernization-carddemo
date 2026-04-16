package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Java mapping of COBOL copybook COCOM01Y — COMMAREA (Communication Area).
 * <pre>
 * 01 CARDDEMO-COMMAREA.
 *   05 CDEMO-GENERAL-INFO.
 *     10 CDEMO-FROM-TRANID        PIC X(04)
 *     10 CDEMO-FROM-PROGRAM       PIC X(08)
 *     10 CDEMO-TO-TRANID          PIC X(04)
 *     10 CDEMO-TO-PROGRAM         PIC X(08)
 *     10 CDEMO-USER-ID            PIC X(08)
 *     10 CDEMO-USER-TYPE          PIC X(01)
 *       88 CDEMO-USRTYP-ADMIN     VALUE 'A'
 *       88 CDEMO-USRTYP-USER      VALUE 'U'
 *     10 CDEMO-PGM-CONTEXT        PIC 9(01)
 *       88 CDEMO-PGM-ENTER        VALUE 0
 *       88 CDEMO-PGM-REENTER      VALUE 1
 *   05 CDEMO-CUSTOMER-INFO.
 *     10 CDEMO-CUST-ID            PIC 9(09)
 *     10 CDEMO-CUST-FNAME         PIC X(25)
 *     10 CDEMO-CUST-MNAME         PIC X(25)
 *     10 CDEMO-CUST-LNAME         PIC X(25)
 *   05 CDEMO-ACCOUNT-INFO.
 *     10 CDEMO-ACCT-ID            PIC 9(11)
 *     10 CDEMO-ACCT-STATUS        PIC X(01)
 *   05 CDEMO-CARD-INFO.
 *     10 CDEMO-CARD-NUM           PIC 9(16)
 *   05 CDEMO-MORE-INFO.
 *     10 CDEMO-LAST-MAP           PIC X(7)
 *     10 CDEMO-LAST-MAPSET        PIC X(7)
 * </pre>
 */
public class CardDemoCommarea {

    // General Info
    @JsonProperty("cdemoFromTranid")
    private String cdemoFromTranid;

    @JsonProperty("cdemoFromProgram")
    private String cdemoFromProgram;

    @JsonProperty("cdemoToTranid")
    private String cdemoToTranid;

    @JsonProperty("cdemoToProgram")
    private String cdemoToProgram;

    @JsonProperty("cdemoUserId")
    private String cdemoUserId;

    @JsonProperty("cdemoUserType")
    private String cdemoUserType;

    @JsonProperty("cdemoPgmContext")
    private int cdemoPgmContext;

    // Customer Info
    @JsonProperty("cdemoCustId")
    private long cdemoCustId;

    @JsonProperty("cdemoCustFname")
    private String cdemoCustFname;

    @JsonProperty("cdemoCustMname")
    private String cdemoCustMname;

    @JsonProperty("cdemoCustLname")
    private String cdemoCustLname;

    // Account Info
    @JsonProperty("cdemoAcctId")
    private long cdemoAcctId;

    @JsonProperty("cdemoAcctStatus")
    private String cdemoAcctStatus;

    // Card Info
    @JsonProperty("cdemoCardNum")
    private long cdemoCardNum;

    // More Info
    @JsonProperty("cdemoLastMap")
    private String cdemoLastMap;

    @JsonProperty("cdemoLastMapset")
    private String cdemoLastMapset;

    public CardDemoCommarea() {
    }

    public String getCdemoFromTranid() {
        return cdemoFromTranid;
    }

    public void setCdemoFromTranid(String cdemoFromTranid) {
        this.cdemoFromTranid = cdemoFromTranid;
    }

    public String getCdemoFromProgram() {
        return cdemoFromProgram;
    }

    public void setCdemoFromProgram(String cdemoFromProgram) {
        this.cdemoFromProgram = cdemoFromProgram;
    }

    public String getCdemoToTranid() {
        return cdemoToTranid;
    }

    public void setCdemoToTranid(String cdemoToTranid) {
        this.cdemoToTranid = cdemoToTranid;
    }

    public String getCdemoToProgram() {
        return cdemoToProgram;
    }

    public void setCdemoToProgram(String cdemoToProgram) {
        this.cdemoToProgram = cdemoToProgram;
    }

    public String getCdemoUserId() {
        return cdemoUserId;
    }

    public void setCdemoUserId(String cdemoUserId) {
        this.cdemoUserId = cdemoUserId;
    }

    public String getCdemoUserType() {
        return cdemoUserType;
    }

    public void setCdemoUserType(String cdemoUserType) {
        this.cdemoUserType = cdemoUserType;
    }

    public int getCdemoPgmContext() {
        return cdemoPgmContext;
    }

    public void setCdemoPgmContext(int cdemoPgmContext) {
        this.cdemoPgmContext = cdemoPgmContext;
    }

    public long getCdemoCustId() {
        return cdemoCustId;
    }

    public void setCdemoCustId(long cdemoCustId) {
        this.cdemoCustId = cdemoCustId;
    }

    public String getCdemoCustFname() {
        return cdemoCustFname;
    }

    public void setCdemoCustFname(String cdemoCustFname) {
        this.cdemoCustFname = cdemoCustFname;
    }

    public String getCdemoCustMname() {
        return cdemoCustMname;
    }

    public void setCdemoCustMname(String cdemoCustMname) {
        this.cdemoCustMname = cdemoCustMname;
    }

    public String getCdemoCustLname() {
        return cdemoCustLname;
    }

    public void setCdemoCustLname(String cdemoCustLname) {
        this.cdemoCustLname = cdemoCustLname;
    }

    public long getCdemoAcctId() {
        return cdemoAcctId;
    }

    public void setCdemoAcctId(long cdemoAcctId) {
        this.cdemoAcctId = cdemoAcctId;
    }

    public String getCdemoAcctStatus() {
        return cdemoAcctStatus;
    }

    public void setCdemoAcctStatus(String cdemoAcctStatus) {
        this.cdemoAcctStatus = cdemoAcctStatus;
    }

    public long getCdemoCardNum() {
        return cdemoCardNum;
    }

    public void setCdemoCardNum(long cdemoCardNum) {
        this.cdemoCardNum = cdemoCardNum;
    }

    public String getCdemoLastMap() {
        return cdemoLastMap;
    }

    public void setCdemoLastMap(String cdemoLastMap) {
        this.cdemoLastMap = cdemoLastMap;
    }

    public String getCdemoLastMapset() {
        return cdemoLastMapset;
    }

    public void setCdemoLastMapset(String cdemoLastMapset) {
        this.cdemoLastMapset = cdemoLastMapset;
    }

    /**
     * Returns the typed UserType enum for this commarea's user type.
     */
    @JsonIgnore
    public UserType getUserType() {
        if (cdemoUserType == null || cdemoUserType.isEmpty()) {
            return null;
        }
        return UserType.fromCode(cdemoUserType);
    }

    /**
     * Returns the typed ProgramContext enum for this commarea's program context.
     */
    @JsonIgnore
    public ProgramContext getProgramContext() {
        return ProgramContext.fromCode(cdemoPgmContext);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDemoCommarea that = (CardDemoCommarea) o;
        return cdemoPgmContext == that.cdemoPgmContext
                && cdemoCustId == that.cdemoCustId
                && cdemoAcctId == that.cdemoAcctId
                && cdemoCardNum == that.cdemoCardNum
                && Objects.equals(cdemoFromTranid, that.cdemoFromTranid)
                && Objects.equals(cdemoFromProgram, that.cdemoFromProgram)
                && Objects.equals(cdemoToTranid, that.cdemoToTranid)
                && Objects.equals(cdemoToProgram, that.cdemoToProgram)
                && Objects.equals(cdemoUserId, that.cdemoUserId)
                && Objects.equals(cdemoUserType, that.cdemoUserType)
                && Objects.equals(cdemoCustFname, that.cdemoCustFname)
                && Objects.equals(cdemoCustMname, that.cdemoCustMname)
                && Objects.equals(cdemoCustLname, that.cdemoCustLname)
                && Objects.equals(cdemoAcctStatus, that.cdemoAcctStatus)
                && Objects.equals(cdemoLastMap, that.cdemoLastMap)
                && Objects.equals(cdemoLastMapset, that.cdemoLastMapset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdemoFromTranid, cdemoFromProgram, cdemoToTranid,
                cdemoToProgram, cdemoUserId, cdemoUserType, cdemoPgmContext,
                cdemoCustId, cdemoCustFname, cdemoCustMname, cdemoCustLname,
                cdemoAcctId, cdemoAcctStatus, cdemoCardNum,
                cdemoLastMap, cdemoLastMapset);
    }

    @Override
    public String toString() {
        return "CardDemoCommarea{" +
                "cdemoFromTranid='" + cdemoFromTranid + '\'' +
                ", cdemoFromProgram='" + cdemoFromProgram + '\'' +
                ", cdemoToTranid='" + cdemoToTranid + '\'' +
                ", cdemoToProgram='" + cdemoToProgram + '\'' +
                ", cdemoUserId='" + cdemoUserId + '\'' +
                ", cdemoUserType='" + cdemoUserType + '\'' +
                ", cdemoPgmContext=" + cdemoPgmContext +
                ", cdemoCustId=" + cdemoCustId +
                ", cdemoCustFname='" + cdemoCustFname + '\'' +
                ", cdemoCustMname='" + cdemoCustMname + '\'' +
                ", cdemoCustLname='" + cdemoCustLname + '\'' +
                ", cdemoAcctId=" + cdemoAcctId +
                ", cdemoAcctStatus='" + cdemoAcctStatus + '\'' +
                ", cdemoCardNum=" + cdemoCardNum +
                ", cdemoLastMap='" + cdemoLastMap + '\'' +
                ", cdemoLastMapset='" + cdemoLastMapset + '\'' +
                '}';
    }
}
