package com.carddemo.customer;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping the COBOL {@code CUSTOMER-RECORD} (copybook
 * {@code app/cpy/CVCUS01Y.cpy}, VSAM CUSTDAT KSDS, record length 500).
 *
 * <p>Field mappings preserve the original COBOL field names and PIC clauses for
 * traceability. The 168-byte trailing {@code FILLER} is intentionally not
 * mapped. This record contains only display fields ({@code PIC X}) and unsigned
 * numerics ({@code PIC 9}); there are no zoned-decimal money fields, so no
 * overpunch sign decoding is required.</p>
 *
 * <p>{@code CUST-SSN PIC 9(09)} is mapped to {@link String} rather than
 * {@link Long} to preserve leading zeros (e.g. {@code "020973888"}).</p>
 */
@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    /** COBOL: {@code CUST-ID PIC 9(09)} — VSAM primary key (offset 0, len 9). */
    @Id
    @Column(name = "cust_id")
    private Long custId;

    /** COBOL: {@code CUST-FIRST-NAME PIC X(25)} — first name (offset 9, len 25). */
    @Column(name = "cust_first_name", length = 25)
    private String custFirstName;

    /** COBOL: {@code CUST-MIDDLE-NAME PIC X(25)} — middle name (offset 34, len 25). */
    @Column(name = "cust_middle_name", length = 25)
    private String custMiddleName;

    /** COBOL: {@code CUST-LAST-NAME PIC X(25)} — last name (offset 59, len 25). */
    @Column(name = "cust_last_name", length = 25)
    private String custLastName;

    /** COBOL: {@code CUST-ADDR-LINE-1 PIC X(50)} — address line 1 (offset 84, len 50). */
    @Column(name = "cust_addr_line_1", length = 50)
    private String custAddrLine1;

    /** COBOL: {@code CUST-ADDR-LINE-2 PIC X(50)} — address line 2 (offset 134, len 50). */
    @Column(name = "cust_addr_line_2", length = 50)
    private String custAddrLine2;

    /** COBOL: {@code CUST-ADDR-LINE-3 PIC X(50)} — address line 3 (offset 184, len 50). */
    @Column(name = "cust_addr_line_3", length = 50)
    private String custAddrLine3;

    /** COBOL: {@code CUST-ADDR-STATE-CD PIC X(02)} — state code (offset 234, len 2). */
    @Column(name = "cust_addr_state_cd", length = 2)
    private String custAddrStateCd;

    /** COBOL: {@code CUST-ADDR-COUNTRY-CD PIC X(03)} — country code (offset 236, len 3). */
    @Column(name = "cust_addr_country_cd", length = 3)
    private String custAddrCountryCd;

    /** COBOL: {@code CUST-ADDR-ZIP PIC X(10)} — ZIP code (offset 239, len 10). */
    @Column(name = "cust_addr_zip", length = 10)
    private String custAddrZip;

    /** COBOL: {@code CUST-PHONE-NUM-1 PIC X(15)} — phone number 1 (offset 249, len 15). */
    @Column(name = "cust_phone_num_1", length = 15)
    private String custPhoneNum1;

    /** COBOL: {@code CUST-PHONE-NUM-2 PIC X(15)} — phone number 2 (offset 264, len 15). */
    @Column(name = "cust_phone_num_2", length = 15)
    private String custPhoneNum2;

    /**
     * COBOL: {@code CUST-SSN PIC 9(09)} — social security number (offset 279, len 9).
     * Mapped to {@link String} to preserve leading zeros.
     */
    @Column(name = "cust_ssn", length = 9)
    private String custSsn;

    /** COBOL: {@code CUST-GOVT-ISSUED-ID PIC X(20)} — government-issued id (offset 288, len 20). */
    @Column(name = "cust_govt_issued_id", length = 20)
    private String custGovtIssuedId;

    /** COBOL: {@code CUST-DOB-YYYY-MM-DD PIC X(10)} — date of birth, YYYY-MM-DD (offset 308, len 10). */
    @Column(name = "cust_dob_yyyy_mm_dd")
    private LocalDate custDobYyyyMmDd;

    /** COBOL: {@code CUST-EFT-ACCOUNT-ID PIC X(10)} — EFT account id (offset 318, len 10). */
    @Column(name = "cust_eft_account_id", length = 10)
    private String custEftAccountId;

    /** COBOL: {@code CUST-PRI-CARD-HOLDER-IND PIC X(01)} — primary card holder Y/N (offset 328, len 1). */
    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String custPriCardHolderInd;

    /** COBOL: {@code CUST-FICO-CREDIT-SCORE PIC 9(03)} — FICO credit score (offset 329, len 3). */
    @Column(name = "cust_fico_credit_score")
    private Integer custFicoCreditScore;
}
