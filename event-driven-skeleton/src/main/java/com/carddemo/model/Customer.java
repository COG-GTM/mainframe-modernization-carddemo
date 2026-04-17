package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Customer master entity.
 *
 * Maps: CUSTOMER-RECORD from CUSTREC.cpy / CVCUS01Y.cpy (RECLN 500)
 * VSAM KSDS key: CUST-ID
 *
 * Read by CBSTM03A (2000-CUSTFILE-GET) for statement header:
 *   customer name, address, phone, FICO score.
 */
@Entity
@Table(name = "customer")
public class Customer {

    /** CUST-ID PIC 9(09) — VSAM primary key */
    @Id
    @Column(name = "cust_id", nullable = false)
    private Long custId;

    /** CUST-FIRST-NAME PIC X(25) */
    @Column(name = "first_name", length = 25)
    private String firstName;

    /** CUST-MIDDLE-NAME PIC X(25) */
    @Column(name = "middle_name", length = 25)
    private String middleName;

    /** CUST-LAST-NAME PIC X(25) */
    @Column(name = "last_name", length = 25)
    private String lastName;

    /** CUST-ADDR-LINE-1 PIC X(50) */
    @Column(name = "addr_line_1", length = 50)
    private String addrLine1;

    /** CUST-ADDR-LINE-2 PIC X(50) */
    @Column(name = "addr_line_2", length = 50)
    private String addrLine2;

    /** CUST-ADDR-LINE-3 PIC X(50) */
    @Column(name = "addr_line_3", length = 50)
    private String addrLine3;

    /** CUST-ADDR-STATE-CD PIC X(02) */
    @Column(name = "addr_state_cd", length = 2)
    private String addrStateCd;

    /** CUST-ADDR-COUNTRY-CD PIC X(03) */
    @Column(name = "addr_country_cd", length = 3)
    private String addrCountryCd;

    /** CUST-ADDR-ZIP PIC X(10) */
    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    /** CUST-PHONE-NUM-1 PIC X(15) */
    @Column(name = "phone_num_1", length = 15)
    private String phoneNum1;

    /** CUST-PHONE-NUM-2 PIC X(15) */
    @Column(name = "phone_num_2", length = 15)
    private String phoneNum2;

    /** CUST-SSN PIC 9(09) */
    @Column(name = "ssn")
    private Long ssn;

    /** CUST-GOVT-ISSUED-ID PIC X(20) */
    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    /** CUST-DOB-YYYYMMDD PIC X(10) */
    @Column(name = "dob", length = 10)
    private String dob;

    /** CUST-EFT-ACCOUNT-ID PIC X(10) */
    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    /** CUST-PRI-CARD-HOLDER-IND PIC X(01) */
    @Column(name = "pri_card_holder_ind", length = 1)
    private String priCardHolderInd;

    /** CUST-FICO-CREDIT-SCORE PIC 9(03) */
    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;

    protected Customer() {
    }

    public Customer(Long custId) {
        this.custId = custId;
    }

    public Long getCustId() {
        return custId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddrLine1() {
        return addrLine1;
    }

    public String getAddrLine2() {
        return addrLine2;
    }

    public String getAddrLine3() {
        return addrLine3;
    }

    public String getAddrStateCd() {
        return addrStateCd;
    }

    public String getAddrCountryCd() {
        return addrCountryCd;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public String getPhoneNum1() {
        return phoneNum1;
    }

    public String getPhoneNum2() {
        return phoneNum2;
    }

    public Long getSsn() {
        return ssn;
    }

    public String getGovtIssuedId() {
        return govtIssuedId;
    }

    public String getDob() {
        return dob;
    }

    public String getEftAccountId() {
        return eftAccountId;
    }

    public String getPriCardHolderInd() {
        return priCardHolderInd;
    }

    public Integer getFicoCreditScore() {
        return ficoCreditScore;
    }
}
