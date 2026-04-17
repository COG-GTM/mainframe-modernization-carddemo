package com.carddemo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps to CDEMO-CUSTOMER-INFO in COCOM01Y.cpy.
 *
 * <pre>
 * 05 CDEMO-CUSTOMER-INFO.
 *   10 CDEMO-CUST-ID             PIC 9(09).
 *   10 CDEMO-CUST-FNAME          PIC X(25).
 *   10 CDEMO-CUST-MNAME          PIC X(25).
 *   10 CDEMO-CUST-LNAME          PIC X(25).
 * </pre>
 */
public class CustomerInfo {

    @JsonProperty("custId")
    private long custId;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;

    public CustomerInfo() {
    }

    public CustomerInfo(long custId, String firstName, String middleName, String lastName) {
        this.custId = custId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
