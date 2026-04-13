package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVCUS01Y.cpy (CUSTOMER-RECORD).
 * Total COBOL record length: 500 bytes.
 * Seed data file: custdata.txt (50 records).
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    /** CUST-ID — PIC 9(09), bytes [0:9]. Customer ID (primary key). */
    @Id
    @Column(name = "cust_id")
    private Long custId;

    /** CUST-FIRST-NAME — PIC X(25), bytes [9:34]. First name. */
    @Column(name = "cust_first_name", length = 25)
    private String custFirstName;

    /** CUST-MIDDLE-NAME — PIC X(25), bytes [34:59]. Middle name. */
    @Column(name = "cust_middle_name", length = 25)
    private String custMiddleName;

    /** CUST-LAST-NAME — PIC X(25), bytes [59:84]. Last name. */
    @Column(name = "cust_last_name", length = 25)
    private String custLastName;

    /** CUST-ADDR-LINE-1 — PIC X(50), bytes [84:134]. Address line 1. */
    @Column(name = "cust_addr_line_1", length = 50)
    private String custAddrLine1;

    /** CUST-ADDR-LINE-2 — PIC X(50), bytes [134:184]. Address line 2. */
    @Column(name = "cust_addr_line_2", length = 50)
    private String custAddrLine2;

    /** CUST-ADDR-LINE-3 — PIC X(50), bytes [184:234]. Address line 3. */
    @Column(name = "cust_addr_line_3", length = 50)
    private String custAddrLine3;

    /** CUST-ADDR-STATE-CD — PIC X(02), bytes [234:236]. State code. */
    @Column(name = "cust_addr_state_cd", length = 2)
    private String custAddrStateCd;

    /** CUST-ADDR-COUNTRY-CD — PIC X(03), bytes [236:239]. Country code. */
    @Column(name = "cust_addr_country_cd", length = 3)
    private String custAddrCountryCd;

    /** CUST-ADDR-ZIP — PIC X(10), bytes [239:249]. ZIP code. */
    @Column(name = "cust_addr_zip", length = 10)
    private String custAddrZip;

    /** CUST-PHONE-NUM-1 — PIC X(15), bytes [249:264]. Primary phone. */
    @Column(name = "cust_phone_num_1", length = 15)
    private String custPhoneNum1;

    /** CUST-PHONE-NUM-2 — PIC X(15), bytes [264:279]. Secondary phone. */
    @Column(name = "cust_phone_num_2", length = 15)
    private String custPhoneNum2;

    /** CUST-SSN — PIC 9(09), bytes [279:288]. Social security number. */
    @Column(name = "cust_ssn")
    private Long custSsn;

    /** CUST-GOVT-ISSUED-ID — PIC X(20), bytes [288:308]. Government-issued ID. */
    @Column(name = "cust_govt_issued_id", length = 20)
    private String custGovtIssuedId;

    /** CUST-DOB-YYYY-MM-DD — PIC X(10), bytes [308:318]. Date of birth (YYYY-MM-DD). */
    @Column(name = "cust_dob")
    private LocalDate custDob;

    /** CUST-EFT-ACCOUNT-ID — PIC X(10), bytes [318:328]. EFT account ID. */
    @Column(name = "cust_eft_account_id", length = 10)
    private String custEftAccountId;

    /** CUST-PRI-CARD-HOLDER-IND — PIC X(01), bytes [328:329]. Primary card holder indicator (Y/N). */
    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String custPriCardHolderInd;

    /** CUST-FICO-CREDIT-SCORE — PIC 9(03), bytes [329:332]. FICO credit score. */
    @Column(name = "cust_fico_credit_score")
    private Integer custFicoCreditScore;

    // FILLER — PIC X(168), bytes [332:500]. Padding — not mapped.
}
