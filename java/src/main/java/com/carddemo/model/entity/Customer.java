package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVCUS01Y (CUSTOMER-RECORD), VSAM file CUSTDATA, RECLN 500.
 */
@Entity
@Table(name = "customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    /** CUST-ID PIC 9(09). */
    @Id
    @Column(name = "cust_id", nullable = false)
    private Long customerId;

    /** CUST-FIRST-NAME PIC X(25). */
    @Column(name = "cust_first_name", length = 25)
    private String firstName;

    /** CUST-MIDDLE-NAME PIC X(25). */
    @Column(name = "cust_middle_name", length = 25)
    private String middleName;

    /** CUST-LAST-NAME PIC X(25). */
    @Column(name = "cust_last_name", length = 25)
    private String lastName;

    /** CUST-ADDR-LINE-1 PIC X(50). */
    @Column(name = "cust_addr_line_1", length = 50)
    private String addressLine1;

    /** CUST-ADDR-LINE-2 PIC X(50). */
    @Column(name = "cust_addr_line_2", length = 50)
    private String addressLine2;

    /** CUST-ADDR-LINE-3 PIC X(50). */
    @Column(name = "cust_addr_line_3", length = 50)
    private String addressLine3;

    /** CUST-ADDR-STATE-CD PIC X(02). */
    @Column(name = "cust_addr_state_cd", length = 2)
    private String stateCode;

    /** CUST-ADDR-COUNTRY-CD PIC X(03). */
    @Column(name = "cust_addr_country_cd", length = 3)
    private String countryCode;

    /** CUST-ADDR-ZIP PIC X(10). */
    @Column(name = "cust_addr_zip", length = 10)
    private String zipCode;

    /** CUST-PHONE-NUM-1 PIC X(15). */
    @Column(name = "cust_phone_num_1", length = 15)
    private String phoneNumber1;

    /** CUST-PHONE-NUM-2 PIC X(15). */
    @Column(name = "cust_phone_num_2", length = 15)
    private String phoneNumber2;

    /** CUST-SSN PIC 9(09). */
    @Column(name = "cust_ssn")
    private Long ssn;

    /** CUST-GOVT-ISSUED-ID PIC X(20). */
    @Column(name = "cust_govt_issued_id", length = 20)
    private String governmentIssuedId;

    /** CUST-DOB-YYYY-MM-DD PIC X(10). */
    @Column(name = "cust_dob", length = 10)
    private String dateOfBirth;

    /** CUST-EFT-ACCOUNT-ID PIC X(10). */
    @Column(name = "cust_eft_account_id", length = 10)
    private String eftAccountId;

    /** CUST-PRI-CARD-HOLDER-IND PIC X(01). */
    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String primaryCardHolderIndicator;

    /** CUST-FICO-CREDIT-SCORE PIC 9(03). */
    @Column(name = "cust_fico_credit_score")
    private Integer ficoCreditScore;
}
