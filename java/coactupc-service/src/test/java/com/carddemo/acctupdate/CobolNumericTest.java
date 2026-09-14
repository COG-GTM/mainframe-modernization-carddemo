package com.carddemo.acctupdate;

import com.carddemo.acctupdate.service.edit.CobolNumeric;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CobolNumericTest {
    @Test
    void parsesPlainMoney() {
        assertEquals(new BigDecimal("1000.00"), CobolNumeric.numvalC("1000.00"));
    }

    @Test
    void parsesCommaSeparatedNegativeMoney() {
        assertEquals(new BigDecimal("-1000.50"), CobolNumeric.numvalC("-1,000.50"));
    }

    @Test
    void parsesCurrencySymbol() {
        assertEquals(new BigDecimal("250.00"), CobolNumeric.numvalC("$250"));
    }

    @Test
    void padsFractionToTwoDigits() {
        assertEquals(new BigDecimal("12.50"), CobolNumeric.numvalC("12.5"));
    }

    @Test
    void truncatesFractionAfterTwoDigits() {
        assertEquals(new BigDecimal("12.34"), CobolNumeric.numvalC("12.345"));
    }

    @Test
    void parsesTrailingNegativeSign() {
        assertEquals(new BigDecimal("-100.00"), CobolNumeric.numvalC("100-"));
    }

    @Test
    void parsesTrailingDebitSign() {
        assertEquals(new BigDecimal("-100.00"), CobolNumeric.numvalC("100DB"));
    }

    @Test
    void rejectsAlphabeticInput() {
        assertFalse(CobolNumeric.testNumvalC("abc"));
    }

    @Test
    void rejectsEmptyInput() {
        assertFalse(CobolNumeric.testNumvalC(""));
    }

    @Test
    void rejectsIntegerOverflow() {
        assertFalse(CobolNumeric.testNumvalC("12345678901.00"));
    }

    @Test
    void rejectsConflictingSigns() {
        assertFalse(CobolNumeric.testNumvalC("+5-"));
    }
}
