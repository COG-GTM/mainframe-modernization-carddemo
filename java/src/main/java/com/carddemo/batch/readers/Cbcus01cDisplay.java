package com.carddemo.batch.readers;

import com.carddemo.model.entity.Customer;
import java.util.List;

/**
 * COBOL program: CBCUS01C — read and print the customer master file (JCL READCUST).
 * Copybook CVCUS01Y (CUSTOMER-RECORD, RECLN 500), VSAM file CUSTDATA.
 *
 * <p>Like CBACT03C the record is displayed twice per read: once in
 * {@code 1000-CUSTFILE-GET-NEXT} and once in the main read loop.
 */
final class Cbcus01cDisplay {

    private Cbcus01cDisplay() {
    }

    static List<String> displayLines(Customer customer) {
        String line = record(customer);
        return List.of(line, line);
    }

    /** The 500 byte CUSTOMER-RECORD as laid out by CVCUS01Y, trailing FILLER included. */
    static String record(Customer customer) {
        return CobolDisplay.number(customer.getCustomerId(), 9)
                + CobolDisplay.text(customer.getFirstName(), 25)
                + CobolDisplay.text(customer.getMiddleName(), 25)
                + CobolDisplay.text(customer.getLastName(), 25)
                + CobolDisplay.text(customer.getAddressLine1(), 50)
                + CobolDisplay.text(customer.getAddressLine2(), 50)
                + CobolDisplay.text(customer.getAddressLine3(), 50)
                + CobolDisplay.text(customer.getStateCode(), 2)
                + CobolDisplay.text(customer.getCountryCode(), 3)
                + CobolDisplay.text(customer.getZipCode(), 10)
                + CobolDisplay.text(customer.getPhoneNumber1(), 15)
                + CobolDisplay.text(customer.getPhoneNumber2(), 15)
                + CobolDisplay.number(customer.getSsn(), 9)
                + CobolDisplay.text(customer.getGovernmentIssuedId(), 20)
                + CobolDisplay.text(customer.getDateOfBirth(), 10)
                + CobolDisplay.text(customer.getEftAccountId(), 10)
                + CobolDisplay.text(customer.getPrimaryCardHolderIndicator(), 1)
                + CobolDisplay.number(customer.getFicoCreditScore(), 3)
                + " ".repeat(168);
    }
}
