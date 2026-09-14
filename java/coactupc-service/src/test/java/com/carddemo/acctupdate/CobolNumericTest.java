package com.carddemo.acctupdate;

import com.carddemo.acctupdate.service.edit.CobolNumeric;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CobolNumericTest {
    @Test void parsesCobolCurrencyForms() {
        assertEquals(new BigDecimal("1000.00"), CobolNumeric.numvalC("1000.00"));
        assertEquals(new BigDecimal("-1000.50"), CobolNumeric.numvalC("-1,000.50"));
        assertEquals(new BigDecimal("250.00"), CobolNumeric.numvalC("$250"));
        assertEquals(new BigDecimal("12.50"), CobolNumeric.numvalC("12.5"));
        assertEquals(new BigDecimal("12.34"), CobolNumeric.numvalC("12.345"));
        assertEquals(new BigDecimal("-100.00"), CobolNumeric.numvalC("100-"));
    }
    @Test void rejectsInvalidAndOverflow() {
        assertFalse(CobolNumeric.testNumvalC("abc"));
        assertFalse(CobolNumeric.testNumvalC(""));
        assertFalse(CobolNumeric.testNumvalC("12345678901.00"));
    }
}
