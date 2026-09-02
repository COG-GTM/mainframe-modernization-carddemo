package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

/**
 * CUSTOMER-RECORD, copybook CVCUS01Y, LRECL 500 (CUSTDATA / custdata.txt).
 */
public class Customer {

    public static final int RECORD_LENGTH = 500;

    private String customerId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String stateCode;
    private String countryCode;
    private String addressZip;
    private String phoneNumber1;
    private String phoneNumber2;
    private String ssn;
    private String governmentIssuedId;
    private String dateOfBirth;
    private String eftAccountId;
    private char primaryCardHolderIndicator;
    private int ficoCreditScore;

    public static Customer parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        Customer customer = new Customer();
        customer.customerId = cursor.fixed(9);
        customer.firstName = cursor.text(25);
        customer.middleName = cursor.text(25);
        customer.lastName = cursor.text(25);
        customer.addressLine1 = cursor.text(50);
        customer.addressLine2 = cursor.text(50);
        customer.addressLine3 = cursor.text(50);
        customer.stateCode = cursor.text(2);
        customer.countryCode = cursor.text(3);
        customer.addressZip = cursor.text(10);
        customer.phoneNumber1 = cursor.text(15);
        customer.phoneNumber2 = cursor.text(15);
        customer.ssn = cursor.fixed(9);
        customer.governmentIssuedId = cursor.text(20);
        customer.dateOfBirth = cursor.text(10);
        customer.eftAccountId = cursor.text(10);
        customer.primaryCardHolderIndicator = cursor.flag();
        customer.ficoCreditScore = cursor.integer(3);
        return customer;
    }

    public String format() {
        return CobolCodec.encodeText(customerId, 9)
                + CobolCodec.encodeText(firstName, 25)
                + CobolCodec.encodeText(middleName, 25)
                + CobolCodec.encodeText(lastName, 25)
                + CobolCodec.encodeText(addressLine1, 50)
                + CobolCodec.encodeText(addressLine2, 50)
                + CobolCodec.encodeText(addressLine3, 50)
                + CobolCodec.encodeText(stateCode, 2)
                + CobolCodec.encodeText(countryCode, 3)
                + CobolCodec.encodeText(addressZip, 10)
                + CobolCodec.encodeText(phoneNumber1, 15)
                + CobolCodec.encodeText(phoneNumber2, 15)
                + CobolCodec.encodeText(ssn, 9)
                + CobolCodec.encodeText(governmentIssuedId, 20)
                + CobolCodec.encodeText(dateOfBirth, 10)
                + CobolCodec.encodeText(eftAccountId, 10)
                + primaryCardHolderIndicator
                + CobolCodec.encodeNumeric(ficoCreditScore, 3)
                + " ".repeat(168);
    }

    public String fullName() {
        return (firstName + " " + middleName + " " + lastName).replaceAll("\\s+", " ").trim();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public String getPhoneNumber1() {
        return phoneNumber1;
    }

    public void setPhoneNumber1(String phoneNumber1) {
        this.phoneNumber1 = phoneNumber1;
    }

    public String getPhoneNumber2() {
        return phoneNumber2;
    }

    public void setPhoneNumber2(String phoneNumber2) {
        this.phoneNumber2 = phoneNumber2;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getGovernmentIssuedId() {
        return governmentIssuedId;
    }

    public void setGovernmentIssuedId(String governmentIssuedId) {
        this.governmentIssuedId = governmentIssuedId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEftAccountId() {
        return eftAccountId;
    }

    public void setEftAccountId(String eftAccountId) {
        this.eftAccountId = eftAccountId;
    }

    public char getPrimaryCardHolderIndicator() {
        return primaryCardHolderIndicator;
    }

    public void setPrimaryCardHolderIndicator(char primaryCardHolderIndicator) {
        this.primaryCardHolderIndicator = primaryCardHolderIndicator;
    }

    public int getFicoCreditScore() {
        return ficoCreditScore;
    }

    public void setFicoCreditScore(int ficoCreditScore) {
        this.ficoCreditScore = ficoCreditScore;
    }

    @Override
    public String toString() {
        return "Customer[" + customerId + ", " + fullName() + "]";
    }
}
