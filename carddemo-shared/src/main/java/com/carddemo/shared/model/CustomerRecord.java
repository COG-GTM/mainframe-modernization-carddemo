package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVCUS01Y — Customer Record (RECLN 500).
 * <pre>
 * 01 CUSTOMER-RECORD.
 *   05 CUST-ID                    PIC 9(09)
 *   05 CUST-FIRST-NAME            PIC X(25)
 *   05 CUST-MIDDLE-NAME           PIC X(25)
 *   05 CUST-LAST-NAME             PIC X(25)
 *   05 CUST-ADDR-LINE-1           PIC X(50)
 *   05 CUST-ADDR-LINE-2           PIC X(50)
 *   05 CUST-ADDR-LINE-3           PIC X(50)
 *   05 CUST-ADDR-STATE-CD         PIC X(02)
 *   05 CUST-ADDR-COUNTRY-CD       PIC X(03)
 *   05 CUST-ADDR-ZIP              PIC X(10)
 *   05 CUST-PHONE-NUM-1           PIC X(15)
 *   05 CUST-PHONE-NUM-2           PIC X(15)
 *   05 CUST-SSN                   PIC 9(09)
 *   05 CUST-GOVT-ISSUED-ID        PIC X(20)
 *   05 CUST-DOB-YYYY-MM-DD        PIC X(10)
 *   05 CUST-EFT-ACCOUNT-ID        PIC X(10)
 *   05 CUST-PRI-CARD-HOLDER-IND   PIC X(01)
 *   05 CUST-FICO-CREDIT-SCORE     PIC 9(03)
 *   05 FILLER                     PIC X(168)
 * </pre>
 */
public class CustomerRecord {

    @JsonProperty("custId")
    private long custId;

    @JsonProperty("custFirstName")
    private String custFirstName;

    @JsonProperty("custMiddleName")
    private String custMiddleName;

    @JsonProperty("custLastName")
    private String custLastName;

    @JsonProperty("custAddrLine1")
    private String custAddrLine1;

    @JsonProperty("custAddrLine2")
    private String custAddrLine2;

    @JsonProperty("custAddrLine3")
    private String custAddrLine3;

    @JsonProperty("custAddrStateCd")
    private String custAddrStateCd;

    @JsonProperty("custAddrCountryCd")
    private String custAddrCountryCd;

    @JsonProperty("custAddrZip")
    private String custAddrZip;

    @JsonProperty("custPhoneNum1")
    private String custPhoneNum1;

    @JsonProperty("custPhoneNum2")
    private String custPhoneNum2;

    @JsonProperty("custSsn")
    private long custSsn;

    @JsonProperty("custGovtIssuedId")
    private String custGovtIssuedId;

    @JsonProperty("custDobYyyyMmDd")
    private String custDobYyyyMmDd;

    @JsonProperty("custEftAccountId")
    private String custEftAccountId;

    @JsonProperty("custPriCardHolderInd")
    private String custPriCardHolderInd;

    @JsonProperty("custFicoCreditScore")
    private int custFicoCreditScore;

    public CustomerRecord() {
    }

    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    public String getCustFirstName() {
        return custFirstName;
    }

    public void setCustFirstName(String custFirstName) {
        this.custFirstName = custFirstName;
    }

    public String getCustMiddleName() {
        return custMiddleName;
    }

    public void setCustMiddleName(String custMiddleName) {
        this.custMiddleName = custMiddleName;
    }

    public String getCustLastName() {
        return custLastName;
    }

    public void setCustLastName(String custLastName) {
        this.custLastName = custLastName;
    }

    public String getCustAddrLine1() {
        return custAddrLine1;
    }

    public void setCustAddrLine1(String custAddrLine1) {
        this.custAddrLine1 = custAddrLine1;
    }

    public String getCustAddrLine2() {
        return custAddrLine2;
    }

    public void setCustAddrLine2(String custAddrLine2) {
        this.custAddrLine2 = custAddrLine2;
    }

    public String getCustAddrLine3() {
        return custAddrLine3;
    }

    public void setCustAddrLine3(String custAddrLine3) {
        this.custAddrLine3 = custAddrLine3;
    }

    public String getCustAddrStateCd() {
        return custAddrStateCd;
    }

    public void setCustAddrStateCd(String custAddrStateCd) {
        this.custAddrStateCd = custAddrStateCd;
    }

    public String getCustAddrCountryCd() {
        return custAddrCountryCd;
    }

    public void setCustAddrCountryCd(String custAddrCountryCd) {
        this.custAddrCountryCd = custAddrCountryCd;
    }

    public String getCustAddrZip() {
        return custAddrZip;
    }

    public void setCustAddrZip(String custAddrZip) {
        this.custAddrZip = custAddrZip;
    }

    public String getCustPhoneNum1() {
        return custPhoneNum1;
    }

    public void setCustPhoneNum1(String custPhoneNum1) {
        this.custPhoneNum1 = custPhoneNum1;
    }

    public String getCustPhoneNum2() {
        return custPhoneNum2;
    }

    public void setCustPhoneNum2(String custPhoneNum2) {
        this.custPhoneNum2 = custPhoneNum2;
    }

    public long getCustSsn() {
        return custSsn;
    }

    public void setCustSsn(long custSsn) {
        this.custSsn = custSsn;
    }

    public String getCustGovtIssuedId() {
        return custGovtIssuedId;
    }

    public void setCustGovtIssuedId(String custGovtIssuedId) {
        this.custGovtIssuedId = custGovtIssuedId;
    }

    public String getCustDobYyyyMmDd() {
        return custDobYyyyMmDd;
    }

    public void setCustDobYyyyMmDd(String custDobYyyyMmDd) {
        this.custDobYyyyMmDd = custDobYyyyMmDd;
    }

    public String getCustEftAccountId() {
        return custEftAccountId;
    }

    public void setCustEftAccountId(String custEftAccountId) {
        this.custEftAccountId = custEftAccountId;
    }

    public String getCustPriCardHolderInd() {
        return custPriCardHolderInd;
    }

    public void setCustPriCardHolderInd(String custPriCardHolderInd) {
        this.custPriCardHolderInd = custPriCardHolderInd;
    }

    public int getCustFicoCreditScore() {
        return custFicoCreditScore;
    }

    public void setCustFicoCreditScore(int custFicoCreditScore) {
        this.custFicoCreditScore = custFicoCreditScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerRecord that = (CustomerRecord) o;
        return custId == that.custId
                && custSsn == that.custSsn
                && custFicoCreditScore == that.custFicoCreditScore
                && Objects.equals(custFirstName, that.custFirstName)
                && Objects.equals(custMiddleName, that.custMiddleName)
                && Objects.equals(custLastName, that.custLastName)
                && Objects.equals(custAddrLine1, that.custAddrLine1)
                && Objects.equals(custAddrLine2, that.custAddrLine2)
                && Objects.equals(custAddrLine3, that.custAddrLine3)
                && Objects.equals(custAddrStateCd, that.custAddrStateCd)
                && Objects.equals(custAddrCountryCd, that.custAddrCountryCd)
                && Objects.equals(custAddrZip, that.custAddrZip)
                && Objects.equals(custPhoneNum1, that.custPhoneNum1)
                && Objects.equals(custPhoneNum2, that.custPhoneNum2)
                && Objects.equals(custGovtIssuedId, that.custGovtIssuedId)
                && Objects.equals(custDobYyyyMmDd, that.custDobYyyyMmDd)
                && Objects.equals(custEftAccountId, that.custEftAccountId)
                && Objects.equals(custPriCardHolderInd, that.custPriCardHolderInd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(custId, custFirstName, custMiddleName, custLastName,
                custAddrLine1, custAddrLine2, custAddrLine3, custAddrStateCd,
                custAddrCountryCd, custAddrZip, custPhoneNum1, custPhoneNum2,
                custSsn, custGovtIssuedId, custDobYyyyMmDd, custEftAccountId,
                custPriCardHolderInd, custFicoCreditScore);
    }

    @Override
    public String toString() {
        return "CustomerRecord{" +
                "custId=" + custId +
                ", custFirstName='" + custFirstName + '\'' +
                ", custMiddleName='" + custMiddleName + '\'' +
                ", custLastName='" + custLastName + '\'' +
                ", custAddrLine1='" + custAddrLine1 + '\'' +
                ", custAddrLine2='" + custAddrLine2 + '\'' +
                ", custAddrLine3='" + custAddrLine3 + '\'' +
                ", custAddrStateCd='" + custAddrStateCd + '\'' +
                ", custAddrCountryCd='" + custAddrCountryCd + '\'' +
                ", custAddrZip='" + custAddrZip + '\'' +
                ", custPhoneNum1='" + custPhoneNum1 + '\'' +
                ", custPhoneNum2='" + custPhoneNum2 + '\'' +
                ", custSsn=" + custSsn +
                ", custGovtIssuedId='" + custGovtIssuedId + '\'' +
                ", custDobYyyyMmDd='" + custDobYyyyMmDd + '\'' +
                ", custEftAccountId='" + custEftAccountId + '\'' +
                ", custPriCardHolderInd='" + custPriCardHolderInd + '\'' +
                ", custFicoCreditScore=" + custFicoCreditScore +
                '}';
    }
}
