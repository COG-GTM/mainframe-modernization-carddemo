package com.carddemo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity derived from COBOL copybook CVCUS01Y.cpy (CUSTOMER-RECORD).
 *
 * <pre>
 * 01  CUSTOMER-RECORD.
 *     05  CUST-ID                    PIC 9(09).
 *     05  CUST-FIRST-NAME            PIC X(25).
 *     05  CUST-MIDDLE-NAME           PIC X(25).
 *     05  CUST-LAST-NAME             PIC X(25).
 *     05  CUST-ADDR-LINE-1           PIC X(50).
 *     05  CUST-ADDR-LINE-2           PIC X(50).
 *     05  CUST-ADDR-LINE-3           PIC X(50).
 *     05  CUST-ADDR-STATE-CD         PIC X(02).
 *     05  CUST-ADDR-COUNTRY-CD       PIC X(03).
 *     05  CUST-ADDR-ZIP              PIC X(10).
 *     05  CUST-PHONE-NUM-1           PIC X(15).
 *     05  CUST-PHONE-NUM-2           PIC X(15).
 *     05  CUST-SSN                   PIC 9(09).
 *     05  CUST-GOVT-ISSUED-ID        PIC X(20).
 *     05  CUST-DOB-YYYY-MM-DD        PIC X(10).
 *     05  CUST-EFT-ACCOUNT-ID        PIC X(10).
 *     05  CUST-PRI-CARD-HOLDER-IND   PIC X(01).
 *     05  CUST-FICO-CREDIT-SCORE     PIC 9(03).
 * </pre>
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "customer_id", nullable = false)
    @NotNull
    private Long customerId;

    @Column(name = "first_name", length = 25)
    @Size(max = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    @Size(max = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    @Size(max = 25)
    private String lastName;

    @Column(name = "address_line_1", length = 50)
    @Size(max = 50)
    private String addressLine1;

    @Column(name = "address_line_2", length = 50)
    @Size(max = 50)
    private String addressLine2;

    @Column(name = "address_line_3", length = 50)
    @Size(max = 50)
    private String addressLine3;

    @Column(name = "state_code", length = 2)
    @Size(max = 2)
    private String stateCode;

    @Column(name = "country_code", length = 3)
    @Size(max = 3)
    private String countryCode;

    @Column(name = "zip_code", length = 10)
    @Size(max = 10)
    private String zipCode;

    @Column(name = "phone_number_1", length = 15)
    @Size(max = 15)
    private String phoneNumber1;

    @Column(name = "phone_number_2", length = 15)
    @Size(max = 15)
    private String phoneNumber2;

    @Column(name = "ssn", length = 9)
    @Size(max = 9)
    private String ssn;

    @Column(name = "govt_issued_id", length = 20)
    @Size(max = 20)
    private String govtIssuedId;

    @Column(name = "date_of_birth", length = 10)
    @Size(max = 10)
    private String dateOfBirth;

    @Column(name = "eft_account_id", length = 10)
    @Size(max = 10)
    private String eftAccountId;

    @Column(name = "pri_card_holder_ind", length = 1)
    @Size(max = 1)
    private String priCardHolderInd;

    @Column(name = "fico_score")
    private Integer ficoScore;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CardCrossReference> cardCrossReferences = new ArrayList<>();
}
