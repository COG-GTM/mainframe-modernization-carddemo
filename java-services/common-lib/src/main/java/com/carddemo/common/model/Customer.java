package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVCUS01Y.cpy — CUSTOMER-RECORD (500 bytes).
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "first_name", length = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    private String lastName;

    @Column(name = "address_line_1", length = 50)
    private String addressLine1;

    @Column(name = "address_line_2", length = 50)
    private String addressLine2;

    @Column(name = "address_line_3", length = 50)
    private String addressLine3;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "phone_number_1", length = 15)
    private String phoneNumber1;

    @Column(name = "phone_number_2", length = 15)
    private String phoneNumber2;

    @Column(name = "ssn")
    private Long ssn;

    @Column(name = "government_issued_id", length = 20)
    private String governmentIssuedId;

    @Column(name = "date_of_birth", length = 10)
    private String dateOfBirth;

    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "primary_card_holder_indicator", length = 1)
    private String primaryCardHolderIndicator;

    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;
}
