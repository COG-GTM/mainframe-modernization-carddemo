package com.carddemo.model;

import java.math.BigDecimal;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Account master record (ACCTDAT / MFE.CARDDEMO.ACCTDATA). Layout: copybook {@code CVACT01Y}, LRECL 300. */
@CobolRecord(copybook = "CVACT01Y", length = 300)
public class Account {

    /** {@code ACCT-ID PIC 9(11)} */
    @CobolField(name = "ACCT-ID", offset = 0, length = 11, type = PicType.UNSIGNED)
    private Long id;

    /** {@code ACCT-ACTIVE-STATUS PIC X(01)} */
    @CobolField(name = "ACCT-ACTIVE-STATUS", offset = 11, length = 1, type = PicType.ALPHANUMERIC)
    private String activeStatus;

    /** {@code ACCT-CURR-BAL PIC S9(10)V99} */
    @CobolField(name = "ACCT-CURR-BAL", offset = 12, length = 12, type = PicType.SIGNED, scale = 2)
    private BigDecimal currBal;

    /** {@code ACCT-CREDIT-LIMIT PIC S9(10)V99} */
    @CobolField(name = "ACCT-CREDIT-LIMIT", offset = 24, length = 12, type = PicType.SIGNED, scale = 2)
    private BigDecimal creditLimit;

    /** {@code ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99} */
    @CobolField(name = "ACCT-CASH-CREDIT-LIMIT", offset = 36, length = 12, type = PicType.SIGNED, scale = 2)
    private BigDecimal cashCreditLimit;

    /** {@code ACCT-OPEN-DATE PIC X(10)} */
    @CobolField(name = "ACCT-OPEN-DATE", offset = 48, length = 10, type = PicType.ALPHANUMERIC)
    private String openDate;

    /** {@code ACCT-EXPIRAION-DATE PIC X(10)} */
    @CobolField(name = "ACCT-EXPIRAION-DATE", offset = 58, length = 10, type = PicType.ALPHANUMERIC)
    private String expiraionDate;

    /** {@code ACCT-REISSUE-DATE PIC X(10)} */
    @CobolField(name = "ACCT-REISSUE-DATE", offset = 68, length = 10, type = PicType.ALPHANUMERIC)
    private String reissueDate;

    /** {@code ACCT-CURR-CYC-CREDIT PIC S9(10)V99} */
    @CobolField(name = "ACCT-CURR-CYC-CREDIT", offset = 78, length = 12, type = PicType.SIGNED, scale = 2)
    private BigDecimal currCycCredit;

    /** {@code ACCT-CURR-CYC-DEBIT PIC S9(10)V99} */
    @CobolField(name = "ACCT-CURR-CYC-DEBIT", offset = 90, length = 12, type = PicType.SIGNED, scale = 2)
    private BigDecimal currCycDebit;

    /** {@code ACCT-ADDR-ZIP PIC X(10)} */
    @CobolField(name = "ACCT-ADDR-ZIP", offset = 102, length = 10, type = PicType.ALPHANUMERIC)
    private String addrZip;

    /** {@code ACCT-GROUP-ID PIC X(10)} */
    @CobolField(name = "ACCT-GROUP-ID", offset = 112, length = 10, type = PicType.ALPHANUMERIC)
    private String groupId;

    /** {@code FILLER PIC X(178)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 122, length = 178, type = PicType.ALPHANUMERIC)
    private String filler;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public BigDecimal getCurrBal() {
        return currBal;
    }

    public void setCurrBal(BigDecimal currBal) {
        this.currBal = currBal;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(BigDecimal cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getExpiraionDate() {
        return expiraionDate;
    }

    public void setExpiraionDate(String expiraionDate) {
        this.expiraionDate = expiraionDate;
    }

    public String getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(String reissueDate) {
        this.reissueDate = reissueDate;
    }

    public BigDecimal getCurrCycCredit() {
        return currCycCredit;
    }

    public void setCurrCycCredit(BigDecimal currCycCredit) {
        this.currCycCredit = currCycCredit;
    }

    public BigDecimal getCurrCycDebit() {
        return currCycDebit;
    }

    public void setCurrCycDebit(BigDecimal currCycDebit) {
        this.currCycDebit = currCycDebit;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
