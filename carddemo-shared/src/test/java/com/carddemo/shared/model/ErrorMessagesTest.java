package com.carddemo.shared.model;

import com.carddemo.shared.constants.CardDemoErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that error message constants are character-for-character identical
 * to the original COBOL source.
 */
class ErrorMessagesTest {

    @Test
    void signOnMessage() {
        assertEquals("Sign On Is Unsuccessful", CardDemoErrorMessages.SIGN_ON_UNSUCCESSFUL);
    }

    @Test
    void transactionProcessingMessages() {
        assertEquals("INVALID CARD NUMBER FOUND", CardDemoErrorMessages.INVALID_CARD_NUMBER_FOUND);
        assertEquals("ACCOUNT RECORD NOT FOUND", CardDemoErrorMessages.ACCOUNT_RECORD_NOT_FOUND);
        assertEquals("CARD RECORD NOT FOUND", CardDemoErrorMessages.CARD_RECORD_NOT_FOUND);
        assertEquals("TRANSACTION TYPE INVALID", CardDemoErrorMessages.TRANSACTION_TYPE_INVALID);
    }

    @Test
    void rejectCodes() {
        assertEquals(100, CardDemoErrorMessages.REJECT_CODE_INVALID_CARD_NUMBER);
        assertEquals(101, CardDemoErrorMessages.REJECT_CODE_ACCOUNT_NOT_FOUND);
        assertEquals(102, CardDemoErrorMessages.REJECT_CODE_CARD_NOT_FOUND);
        assertEquals(103, CardDemoErrorMessages.REJECT_CODE_TRANSACTION_TYPE_INVALID);
    }

    @Test
    void dateValidationMessages() {
        assertEquals("Year must be supplied.", CardDemoErrorMessages.DATE_YEAR_MUST_BE_SUPPLIED);
        assertEquals("must be 4 digit number.", CardDemoErrorMessages.DATE_YEAR_MUST_BE_4_DIGIT);
        assertEquals("Century is not valid.", CardDemoErrorMessages.DATE_CENTURY_NOT_VALID);
        assertEquals("Month must be supplied.", CardDemoErrorMessages.DATE_MONTH_MUST_BE_SUPPLIED);
        assertEquals("Month must be a number between 1 and 12.",
                CardDemoErrorMessages.DATE_MONTH_MUST_BE_1_TO_12);
        assertEquals("Day must be supplied.", CardDemoErrorMessages.DATE_DAY_MUST_BE_SUPPLIED);
        assertEquals("day must be a number between 1 and 31.",
                CardDemoErrorMessages.DATE_DAY_MUST_BE_1_TO_31);
        assertEquals("Cannot have 31 days in this month.",
                CardDemoErrorMessages.DATE_CANNOT_HAVE_31_DAYS);
        assertEquals("Cannot have 30 days in this month.",
                CardDemoErrorMessages.DATE_CANNOT_HAVE_30_DAYS);
        assertEquals("Not a leap year.Cannot have 29 days in this month.",
                CardDemoErrorMessages.DATE_NOT_LEAP_YEAR_29_DAYS);
        assertEquals("cannot be in the future",
                CardDemoErrorMessages.DATE_CANNOT_BE_IN_FUTURE);
    }
}
