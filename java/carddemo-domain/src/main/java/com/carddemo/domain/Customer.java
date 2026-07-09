package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Customer entity — maps COBOL copybook {@code CVCUS01Y} (CUSTOMER-RECORD, RECLN 500).
 *
 * <p>VSAM KSDS keyed on {@code CUST-ID} (see {@code CBCUS01C}: RECORD KEY IS FD-CUST-ID).</p>
 */
@Entity
@Table(name = "customer")
public class Customer {

    /** CUST-ID PIC 9(09) — 9-char zero-padded id, not used in arithmetic. */
    @Id
    @Column(name = "cust_id", length = 9, nullable = false)
    private String custId;

    /** CUST-FIRST-NAME PIC X(25). */
    @Column(name = "cust_first_name", length = 25)
    private String custFirstName;

    /** CUST-MIDDLE-NAME PIC X(25). */
    @Column(name = "cust_middle_name", length = 25)
    private String custMiddleName;

    /** CUST-LAST-NAME PIC X(25). */
    @Column(name = "cust_last_name", length = 25)
    private String custLastName;

    /** CUST-ADDR-LINE-1 PIC X(50). */
    @Column(name = "cust_addr_line_1", length = 50)
    private String custAddrLine1;

    /** CUST-ADDR-LINE-2 PIC X(50). */
    @Column(name = "cust_addr_line_2", length = 50)
    private String custAddrLine2;

    /** CUST-ADDR-LINE-3 PIC X(50). */
    @Column(name = "cust_addr_line_3", length = 50)
    private String custAddrLine3;

    /** CUST-ADDR-STATE-CD PIC X(02). */
    @Column(name = "cust_addr_state_cd", length = 2)
    private String custAddrStateCd;

    /** CUST-ADDR-COUNTRY-CD PIC X(03). */
    @Column(name = "cust_addr_country_cd", length = 3)
    private String custAddrCountryCd;

    /** CUST-ADDR-ZIP PIC X(10). */
    @Column(name = "cust_addr_zip", length = 10)
    private String custAddrZip;

    /** CUST-PHONE-NUM-1 PIC X(15). */
    @Column(name = "cust_phone_num_1", length = 15)
    private String custPhoneNum1;

    /** CUST-PHONE-NUM-2 PIC X(15). */
    @Column(name = "cust_phone_num_2", length = 15)
    private String custPhoneNum2;

    /** CUST-SSN PIC 9(09) — kept as String to preserve leading zeros. */
    @Column(name = "cust_ssn", length = 9)
    private String custSsn;

    /** CUST-GOVT-ISSUED-ID PIC X(20). */
    @Column(name = "cust_govt_issued_id", length = 20)
    private String custGovtIssuedId;

    /** CUST-DOB-YYYY-MM-DD PIC X(10). */
    @Column(name = "cust_dob_yyyy_mm_dd", length = 10)
    private String custDobYyyyMmDd;

    /** CUST-EFT-ACCOUNT-ID PIC X(10). */
    @Column(name = "cust_eft_account_id", length = 10)
    private String custEftAccountId;

    /** CUST-PRI-CARD-HOLDER-IND PIC X(01). */
    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String custPriCardHolderInd;

    /** CUST-FICO-CREDIT-SCORE PIC 9(03) — numeric score. */
    @Column(name = "cust_fico_credit_score")
    private Integer custFicoCreditScore;

    public Customer() {
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
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

    public String getCustSsn() {
        return custSsn;
    }

    public void setCustSsn(String custSsn) {
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

    public Integer getCustFicoCreditScore() {
        return custFicoCreditScore;
    }

    public void setCustFicoCreditScore(Integer custFicoCreditScore) {
        this.custFicoCreditScore = custFicoCreditScore;
    }
}
