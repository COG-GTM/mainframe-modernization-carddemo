package com.carddemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer entity mapped from COBOL copybook CVCUS01Y (CUSTOMER-RECORD).
 * Original COBOL record length: 500 bytes.
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "first_name", length = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    private String lastName;

    @Column(name = "addr_line_1", length = 50)
    private String addrLine1;

    @Column(name = "addr_line_2", length = 50)
    private String addrLine2;

    @Column(name = "addr_line_3", length = 50)
    private String addrLine3;

    @Column(name = "addr_state_cd", length = 2)
    private String addrStateCd;

    @Column(name = "addr_country_cd", length = 3)
    private String addrCountryCd;

    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    @Column(name = "phone_num_1", length = 15)
    private String phoneNum1;

    @Column(name = "phone_num_2", length = 15)
    private String phoneNum2;

    @Column(name = "ssn")
    private Long ssn;

    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    @Column(name = "dob", length = 10)
    private String dateOfBirth;

    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "pri_card_holder_ind", length = 1)
    private String primaryCardHolderInd;

    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;
}
