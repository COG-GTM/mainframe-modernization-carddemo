package com.carddemo.integration;

import com.carddemo.entity.*;
import com.carddemo.mq.AuthorizationRequestListener;
import com.carddemo.repository.*;
import com.carddemo.service.AuthorizationDecisionService;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationRequest;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MqListenerIntegrationTest {
    @Autowired private AuthorizationDecisionService authorizationDecisionService;
    @Autowired private AuthorizationRequestListener listener;
    @Autowired private AuthSummaryRepository authSummaryRepository;
    @Autowired private AuthDetailRepository authDetailRepository;
    @Autowired private CardAccountXrefRepository xrefRepository;
    @Autowired private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        if (accountRepository.findById(10000000001L).isEmpty()) {
            Account acct = new Account();
            acct.setAcctId(10000000001L);
            acct.setActiveStatus("Y");
            acct.setCurrBal(new BigDecimal("1500.00"));
            acct.setCreditLimit(new BigDecimal("10000.00"));
            acct.setCashCreditLimit(new BigDecimal("2000.00"));
            acct.setCurrCycCredit(BigDecimal.ZERO);
            acct.setCurrCycDebit(BigDecimal.ZERO);
            accountRepository.save(acct);
        }
        if (xrefRepository.findByCardNum("4111111111111111").isEmpty()) {
            CardAccountXref xref = new CardAccountXref();
            xref.setCardNum("4111111111111111");
            xref.setAcctId(10000000001L);
            xref.setCustId(100000001L);
            xrefRepository.save(xref);
        }
    }

    @Test
    void authorizationFlow_approve() {
        String csvMessage = "4111111111111111,AUTH,1230,0100,ONLINE,003000,100.00,5411,MERCH001,TEST STORE,NEW YORK,TXN001";
        AuthorizationRequest request = listener.parseCsvMessage(csvMessage);
        assertNotNull(request);
        assertEquals("4111111111111111", request.getCardNum());

        AuthorizationResult result = authorizationDecisionService.processAuthorization(request);
        assertTrue(result.isApproved());
        assertEquals("00", result.getRespCode());
        assertEquals("0000", result.getRespReason());
        assertEquals(new BigDecimal("100.00"), result.getApprovedAmt());
        assertNotNull(result.getSummaryId());

        // Verify DB persistence
        assertTrue(authSummaryRepository.findByCardNum("4111111111111111").size() > 0);
    }

    @Test
    void authorizationFlow_decline_unknownCard() {
        long countBefore = authSummaryRepository.count();
        String csvMessage = "0000000000000000,AUTH,1230,0100,ONLINE,003000,100.00,5411,MERCH001,TEST STORE,NEW YORK,TXN002";
        AuthorizationRequest request = listener.parseCsvMessage(csvMessage);

        AuthorizationResult result = authorizationDecisionService.processAuthorization(request);
        assertFalse(result.isApproved());
        assertEquals("05", result.getRespCode());
        assertEquals("3100", result.getRespReason());
        assertEquals(BigDecimal.ZERO, result.getApprovedAmt());

        assertTrue(authSummaryRepository.count() > countBefore);
    }

    @Test
    void authorizationFlow_csvParsing() {
        String csv = "4111111111111111,AUTH,1230,0100,ONLINE,003000,250.50,5411,MERCH001,TEST STORE,NEW YORK,TXN003";
        AuthorizationRequest request = listener.parseCsvMessage(csv);
        assertEquals("4111111111111111", request.getCardNum());
        assertEquals("AUTH", request.getAuthType());
        assertEquals("1230", request.getCardExpiryDate());
        assertEquals("0100", request.getMessageType());
        assertEquals("ONLINE", request.getMessageSource());
        assertEquals("003000", request.getProcessingCode());
        assertEquals(new BigDecimal("250.50"), request.getTransactionAmt());
        assertEquals("5411", request.getMerchantCategoryCode());
        assertEquals("MERCH001", request.getMerchantId());
        assertEquals("TEST STORE", request.getMerchantName());
        assertEquals("NEW YORK", request.getMerchantCity());
        assertEquals("TXN003", request.getTransactionId());
    }
}
