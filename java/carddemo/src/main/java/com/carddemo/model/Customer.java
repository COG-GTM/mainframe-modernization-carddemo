package com.carddemo.model;

import com.carddemo.model.codec.CobolField;
import com.carddemo.model.codec.CobolRecord;
import com.carddemo.model.codec.PicType;

/** Customer master record (CUSTDAT / MFE.CARDDEMO.CUSTDATA). Layout: copybook {@code CVCUS01Y}, LRECL 500. */
@CobolRecord(copybook = "CVCUS01Y", length = 500)
public class Customer {

    /** {@code CUST-ID PIC 9(09)} */
    @CobolField(name = "CUST-ID", offset = 0, length = 9, type = PicType.UNSIGNED)
    private Long id;

    /** {@code CUST-FIRST-NAME PIC X(25)} */
    @CobolField(name = "CUST-FIRST-NAME", offset = 9, length = 25, type = PicType.ALPHANUMERIC)
    private String firstName;

    /** {@code CUST-MIDDLE-NAME PIC X(25)} */
    @CobolField(name = "CUST-MIDDLE-NAME", offset = 34, length = 25, type = PicType.ALPHANUMERIC)
    private String middleName;

    /** {@code CUST-LAST-NAME PIC X(25)} */
    @CobolField(name = "CUST-LAST-NAME", offset = 59, length = 25, type = PicType.ALPHANUMERIC)
    private String lastName;

    /** {@code CUST-ADDR-LINE-1 PIC X(50)} */
    @CobolField(name = "CUST-ADDR-LINE-1", offset = 84, length = 50, type = PicType.ALPHANUMERIC)
    private String addrLine1;

    /** {@code CUST-ADDR-LINE-2 PIC X(50)} */
    @CobolField(name = "CUST-ADDR-LINE-2", offset = 134, length = 50, type = PicType.ALPHANUMERIC)
    private String addrLine2;

    /** {@code CUST-ADDR-LINE-3 PIC X(50)} */
    @CobolField(name = "CUST-ADDR-LINE-3", offset = 184, length = 50, type = PicType.ALPHANUMERIC)
    private String addrLine3;

    /** {@code CUST-ADDR-STATE-CD PIC X(02)} */
    @CobolField(name = "CUST-ADDR-STATE-CD", offset = 234, length = 2, type = PicType.ALPHANUMERIC)
    private String addrStateCd;

    /** {@code CUST-ADDR-COUNTRY-CD PIC X(03)} */
    @CobolField(name = "CUST-ADDR-COUNTRY-CD", offset = 236, length = 3, type = PicType.ALPHANUMERIC)
    private String addrCountryCd;

    /** {@code CUST-ADDR-ZIP PIC X(10)} */
    @CobolField(name = "CUST-ADDR-ZIP", offset = 239, length = 10, type = PicType.ALPHANUMERIC)
    private String addrZip;

    /** {@code CUST-PHONE-NUM-1 PIC X(15)} */
    @CobolField(name = "CUST-PHONE-NUM-1", offset = 249, length = 15, type = PicType.ALPHANUMERIC)
    private String phoneNum1;

    /** {@code CUST-PHONE-NUM-2 PIC X(15)} */
    @CobolField(name = "CUST-PHONE-NUM-2", offset = 264, length = 15, type = PicType.ALPHANUMERIC)
    private String phoneNum2;

    /** {@code CUST-SSN PIC 9(09)} */
    @CobolField(name = "CUST-SSN", offset = 279, length = 9, type = PicType.UNSIGNED)
    private Long ssn;

    /** {@code CUST-GOVT-ISSUED-ID PIC X(20)} */
    @CobolField(name = "CUST-GOVT-ISSUED-ID", offset = 288, length = 20, type = PicType.ALPHANUMERIC)
    private String govtIssuedId;

    /** {@code CUST-DOB-YYYY-MM-DD PIC X(10)} */
    @CobolField(name = "CUST-DOB-YYYY-MM-DD", offset = 308, length = 10, type = PicType.ALPHANUMERIC)
    private String dobYyyyMmDd;

    /** {@code CUST-EFT-ACCOUNT-ID PIC X(10)} */
    @CobolField(name = "CUST-EFT-ACCOUNT-ID", offset = 318, length = 10, type = PicType.ALPHANUMERIC)
    private String eftAccountId;

    /** {@code CUST-PRI-CARD-HOLDER-IND PIC X(01)} */
    @CobolField(name = "CUST-PRI-CARD-HOLDER-IND", offset = 328, length = 1, type = PicType.ALPHANUMERIC)
    private String priCardHolderInd;

    /** {@code CUST-FICO-CREDIT-SCORE PIC 9(03)} */
    @CobolField(name = "CUST-FICO-CREDIT-SCORE", offset = 329, length = 3, type = PicType.UNSIGNED)
    private Integer ficoCreditScore;

    /** {@code FILLER PIC X(168)}, kept so record images round trip unchanged. */
    @CobolField(name = "FILLER", offset = 332, length = 168, type = PicType.ALPHANUMERIC)
    private String filler;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddrLine1() {
        return addrLine1;
    }

    public void setAddrLine1(String addrLine1) {
        this.addrLine1 = addrLine1;
    }

    public String getAddrLine2() {
        return addrLine2;
    }

    public void setAddrLine2(String addrLine2) {
        this.addrLine2 = addrLine2;
    }

    public String getAddrLine3() {
        return addrLine3;
    }

    public void setAddrLine3(String addrLine3) {
        this.addrLine3 = addrLine3;
    }

    public String getAddrStateCd() {
        return addrStateCd;
    }

    public void setAddrStateCd(String addrStateCd) {
        this.addrStateCd = addrStateCd;
    }

    public String getAddrCountryCd() {
        return addrCountryCd;
    }

    public void setAddrCountryCd(String addrCountryCd) {
        this.addrCountryCd = addrCountryCd;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getPhoneNum1() {
        return phoneNum1;
    }

    public void setPhoneNum1(String phoneNum1) {
        this.phoneNum1 = phoneNum1;
    }

    public String getPhoneNum2() {
        return phoneNum2;
    }

    public void setPhoneNum2(String phoneNum2) {
        this.phoneNum2 = phoneNum2;
    }

    public Long getSsn() {
        return ssn;
    }

    public void setSsn(Long ssn) {
        this.ssn = ssn;
    }

    public String getGovtIssuedId() {
        return govtIssuedId;
    }

    public void setGovtIssuedId(String govtIssuedId) {
        this.govtIssuedId = govtIssuedId;
    }

    public String getDobYyyyMmDd() {
        return dobYyyyMmDd;
    }

    public void setDobYyyyMmDd(String dobYyyyMmDd) {
        this.dobYyyyMmDd = dobYyyyMmDd;
    }

    public String getEftAccountId() {
        return eftAccountId;
    }

    public void setEftAccountId(String eftAccountId) {
        this.eftAccountId = eftAccountId;
    }

    public String getPriCardHolderInd() {
        return priCardHolderInd;
    }

    public void setPriCardHolderInd(String priCardHolderInd) {
        this.priCardHolderInd = priCardHolderInd;
    }

    public Integer getFicoCreditScore() {
        return ficoCreditScore;
    }

    public void setFicoCreditScore(Integer ficoCreditScore) {
        this.ficoCreditScore = ficoCreditScore;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
