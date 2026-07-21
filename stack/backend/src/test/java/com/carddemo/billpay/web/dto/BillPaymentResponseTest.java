package com.carddemo.billpay.web.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Balance display parity with COBOL PIC +9999999999.99 (14 chars). */
class BillPaymentResponseTest {

    @Test
    void formatsPositiveBalance() {
        assertThat(BillPaymentResponse.formatBalance(new BigDecimal("123.45"))).isEqualTo("+0000000123.45");
    }

    @Test
    void formatsZero() {
        assertThat(BillPaymentResponse.formatBalance(new BigDecimal("0.00"))).isEqualTo("+0000000000.00");
    }

    @Test
    void formatsNegativeBalance() {
        assertThat(BillPaymentResponse.formatBalance(new BigDecimal("-42.10"))).isEqualTo("-0000000042.10");
    }

    @Test
    void nullBalanceIsEmpty() {
        assertThat(BillPaymentResponse.formatBalance(null)).isEmpty();
    }
}
