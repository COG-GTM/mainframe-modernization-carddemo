package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVCUS01Y.cpy (CUSTOMER-RECORD, RECLN 500).
 *
 * <pre>
 * COBOL Field                    PIC Clause   Java Type
 * ─────────────────────────────────────────────────────
 * CUST-ID                        PIC 9(09)    Long (@Id)
 * CUST-FIRST-NAME                PIC X(25)    String
 * CUST-MIDDLE-NAME               PIC X(25)    String
 * CUST-LAST-NAME                 PIC X(25)    String
 * CUST-ADDR-LINE-1               PIC X(50)    String
 * CUST-ADDR-LINE-2               PIC X(50)    String
 * CUST-ADDR-LINE-3               PIC X(50)    String
 * CUST-ADDR-STATE-CD             PIC X(02)    String
 * CUST-ADDR-COUNTRY-CD           PIC X(03)    String
 * CUST-ADDR-ZIP                  PIC X(10)    String
 * CUST-PHONE-NUM-1               PIC X(15)    String
 * CUST-PHONE-NUM-2               PIC X(15)    String
 * CUST-SSN                       PIC 9(09)    Long
 * CUST-GOVT-ISSUED-ID            PIC X(20)    String
 * CUST-DOB-YYYY-MM-DD            PIC X(10)    LocalDate
 * CUST-EFT-ACCOUNT-ID            PIC X(10)    String
 * CUST-PRI-CARD-HOLDER-IND       PIC X(01)    String
 * CUST-FICO-CREDIT-SCORE         PIC 9(03)    Integer
 * FILLER                         PIC X(168)   (not mapped)
 * </pre>
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    /** CUST-ID — PIC 9(09) */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /** CUST-FIRST-NAME — PIC X(25) */
    @Column(name = "first_name", length = 25)
    private String firstName;

    /** CUST-MIDDLE-NAME — PIC X(25) */
    @Column(name = "middle_name", length = 25)
    private String middleName;

    /** CUST-LAST-NAME — PIC X(25) */
    @Column(name = "last_name", length = 25)
    private String lastName;

    /** CUST-ADDR-LINE-1 — PIC X(50) */
    @Column(name = "address_line1", length = 50)
    private String addressLine1;

    /** CUST-ADDR-LINE-2 — PIC X(50) */
    @Column(name = "address_line2", length = 50)
    private String addressLine2;

    /** CUST-ADDR-LINE-3 — PIC X(50) */
    @Column(name = "address_line3", length = 50)
    private String addressLine3;

    /** CUST-ADDR-STATE-CD — PIC X(02) */
    @Column(name = "state_code", length = 2)
    private String stateCode;

    /** CUST-ADDR-COUNTRY-CD — PIC X(03) */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    /** CUST-ADDR-ZIP — PIC X(10) */
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /** CUST-PHONE-NUM-1 — PIC X(15) */
    @Column(name = "phone_number1", length = 15)
    private String phoneNumber1;

    /** CUST-PHONE-NUM-2 — PIC X(15) */
    @Column(name = "phone_number2", length = 15)
    private String phoneNumber2;

    /** CUST-SSN — PIC 9(09) */
    @Column(name = "ssn")
    private Long ssn;

    /** CUST-GOVT-ISSUED-ID — PIC X(20) */
    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    /** CUST-DOB-YYYY-MM-DD — PIC X(10) */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** CUST-EFT-ACCOUNT-ID — PIC X(10) */
    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    /** CUST-PRI-CARD-HOLDER-IND — PIC X(01) */
    @Column(name = "primary_card_holder_indicator", length = 1)
    private String primaryCardHolderIndicator;

    /** CUST-FICO-CREDIT-SCORE — PIC 9(03) */
    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;
}
