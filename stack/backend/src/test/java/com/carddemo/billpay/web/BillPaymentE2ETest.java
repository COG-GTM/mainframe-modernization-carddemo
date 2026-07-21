package com.carddemo.billpay.web;

import com.carddemo.billpay.domain.AccountEntity;
import com.carddemo.billpay.domain.CardXrefEntity;
import com.carddemo.billpay.repository.AccountRepository;
import com.carddemo.billpay.repository.CardXrefRepository;
import com.carddemo.billpay.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow (Phase 4): drive the REST boundary and assert BOTH the API response
 * AND the persisted DB rows (transaction inserted + account balance zeroed).
 */
@SpringBootTest
@AutoConfigureMockMvc
class BillPaymentE2ETest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired CardXrefRepository cardXrefRepository;
    @Autowired TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();

        AccountEntity a = new AccountEntity();
        a.setAcctId(11L);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal("250.00"));
        accountRepository.save(a);

        CardXrefEntity x = new CardXrefEntity();
        x.setCardNum("4111111111111111");
        x.setCustId(1L);
        x.setAcctId(11L);
        cardXrefRepository.save(x);
    }

    @Test
    void inquiry_thenPay_persistsRowAndZeroesBalance() throws Exception {
        // Inquiry shows the balance and asks for confirmation (no side effects).
        mockMvc.perform(post("/api/billpay/inquiry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"acctId\":\"11\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageType").value("INFO"))
                .andExpect(jsonPath("$.message").value("Confirm to make a bill payment..."))
                .andExpect(jsonPath("$.balanceDisplay").value("+0000000250.00"));

        assertThat(transactionRepository.count()).isZero();

        // Confirmed payment: response is SUCCESS and the DB reflects the money movement.
        mockMvc.perform(post("/api/billpay/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"acctId\":\"11\",\"confirm\":\"Y\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageType").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Payment successful.  Your Transaction ID is 0000000000000001."))
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.balanceDisplay").value("+0000000000.00"));

        // Persisted DB assertions.
        assertThat(accountRepository.findById(11L).orElseThrow().getCurrBal()).isEqualByComparingTo("0.00");
        assertThat(transactionRepository.count()).isEqualTo(1);
        var txn = transactionRepository.findById("0000000000000001").orElseThrow();
        assertThat(txn.getAmount()).isEqualByComparingTo("250.00");
        assertThat(txn.getDescription()).isEqualTo("BILL PAYMENT - ONLINE");
        assertThat(txn.getCardNum()).isEqualTo("4111111111111111");
    }
}
